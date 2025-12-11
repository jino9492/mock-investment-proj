package com.dongyang.mock.service.api;

import com.dongyang.mock.model.DTO.RankingItemDTO;
import com.dongyang.mock.model.DTO.RealtimeDTO;
import com.dongyang.mock.service.auth.AuthService;
import com.dongyang.mock.util.api.InmemoryCacheManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WebSocketService extends WebSocketClient {
    private final AuthService authService = new AuthService();
    String ACCESS_TOKEN = authService.issueAccessToken();
    private static final Gson gson = new Gson();
    public static final String REALTIME_CACHE_KEY = "stock:realtime:";
    private final InmemoryCacheManager cacheManager = InmemoryCacheManager.getInstance();

    public WebSocketService(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("서버와 연결되었습니다.");
        System.out.println(ACCESS_TOKEN);
        // 로그인 메시지 전송
        JsonObject loginMessage = new JsonObject();
        loginMessage.addProperty("trnm", "LOGIN");
        loginMessage.addProperty("token", ACCESS_TOKEN.replaceAll("Bearer ", ""));
        sendMessage(loginMessage);
    }

    @Override
    public void onMessage(String message) {
        try {
            JsonObject response = gson.fromJson(message, JsonObject.class);
            String trnm = response.has("trnm") ? response.get("trnm").getAsString() : "";

            if ("LOGIN".equals(trnm)) {
                if (!"0".equals(response.get("return_code").getAsString())) {
                    System.out.println("로그인 실패: " + response.get("return_msg").getAsString());
                    close();
                } else {
                    System.out.println("로그인 성공");
                }
            } else if ("PING".equals(trnm)) {
                sendMessage(response); // 그대로 응답
            }

            if (!"PING".equals(trnm)) {
                if (response.has("data"))
                    cacheManager.put(REALTIME_CACHE_KEY + extractItemCode(response), parseResponse(response));
                System.out.println("실시간 시세 서버 응답: " + response.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("연결 종료: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("WebSocket 오류 발생: " + ex.getMessage());
    }

    public void sendMessage(JsonObject message) {
        if (this.isOpen()) {
            try {
                String jsonMessage = gson.toJson(message);
                this.send(jsonMessage);
                System.out.println("Message sent: " + jsonMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private RealtimeDTO parseResponse(JsonObject response) {

        if (!response.has("data") || !response.get("data").isJsonArray()) {
            System.err.println("파싱 실패: 'data' 필드가 없습니다.");
            return null;
        }

        JsonArray dataArray = response.getAsJsonArray("data");

        for (JsonElement element : dataArray) {
            if (element.isJsonObject()) {
                JsonObject itemObject = element.getAsJsonObject();

                String itemCode = itemObject.has("item") ? itemObject.get("item").getAsString() : null;
                if (itemCode == null || !itemObject.has("values") || !itemObject.get("values").isJsonObject()) {
                    continue;
                }

                JsonObject values = itemObject.getAsJsonObject("values");

                try {
                    String curTime = values.get("20").getAsString();
                    String currentPrice = values.get("10").getAsString();
                    String changePrice = values.get("11").getAsString();
                    String fluRt = values.get("12").getAsString();

                    return new RealtimeDTO(
                            curTime,
                            currentPrice,
                            changePrice,
                            fluRt
                    );

                } catch (Exception e) {
                    System.err.println("종목 " + itemCode + " 데이터 변환 중 오류 발생: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private String extractItemCode(JsonObject response) {
        if (!response.has("data") || !response.get("data").isJsonArray()) {
            return null;
        }

        JsonArray dataArray = response.getAsJsonArray("data");
        if (dataArray.size() == 0) {
            return null;
        }

        JsonElement firstElement = dataArray.get(0);
        if (firstElement.isJsonObject()) {
            JsonObject itemObject = firstElement.getAsJsonObject();
            if (itemObject.has("item") && itemObject.get("item").isJsonPrimitive()) {
                return itemObject.get("item").getAsString();
            }
        }
        return null;
    }
}
