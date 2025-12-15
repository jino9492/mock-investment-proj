package com.dongyang.mock.service.api;

import com.dongyang.mock.model.DTO.RealtimeDTO;
import com.dongyang.mock.service.auth.AuthService;
import com.dongyang.mock.util.api.InmemoryCacheManager;
import com.dongyang.mock.websocket.RealtimeWebSocket;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.math.BigDecimal;
import java.net.URI;
import java.util.*;

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
                // onMessage(generateRealtimeJson());
                sendMessage(response); // 그대로 응답
            }

            if (!"PING".equals(trnm)) {
                if (response.has("data")) {
                    System.out.println(extractItemCode(response) + " " + parseResponse(response));
                    cacheManager.put(REALTIME_CACHE_KEY + extractItemCode(response), parseResponse(response));
                }
                // System.out.println("실시간 시세 서버 응답: " + response.toString());
            }

            RealtimeWebSocket.broadcast(message);
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

    // TEST CODE
    public static String generateRealtimeJson() {
        // Gson 인스턴스 생성
        Gson gson = new Gson();

        // 1. 가장 안쪽의 'values' Map 생성
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("20", "165208");
        valuesMap.put("10", "-21200");
        valuesMap.put("11", "-50");
        valuesMap.put("12", "-0.24");
        valuesMap.put("27", "-20800");
        valuesMap.put("28", "-20700");
        valuesMap.put("15", "+82");
        valuesMap.put("13", "30379732");
        valuesMap.put("14", "632640");
        valuesMap.put("16", "20850");
        valuesMap.put("17", "+21150");
        valuesMap.put("18", "-20450");
        valuesMap.put("25", "5");
        valuesMap.put("26", "-1057122");
        valuesMap.put("29", "-22041267850");
        valuesMap.put("30", "-96.64");
        valuesMap.put("31", "36.67");
        valuesMap.put("32", "44");
        valuesMap.put("228", "98.92");
        valuesMap.put("311", "17230");
        valuesMap.put("290", "2");
        valuesMap.put("691", "0");
        valuesMap.put("567", "000000");
        valuesMap.put("568", "000000");
        valuesMap.put("851", "");
        valuesMap.put("1890", "");
        valuesMap.put("1891", "");
        valuesMap.put("1892", "");
        valuesMap.put("1030", "");
        valuesMap.put("1031", "");
        valuesMap.put("1032", "");
        valuesMap.put("1071", "");
        valuesMap.put("1072", "");
        valuesMap.put("1313", "");
        valuesMap.put("1315", "");
        valuesMap.put("1316", "");
        valuesMap.put("1314", "");
        valuesMap.put("1497", "");
        valuesMap.put("1498", "");
        valuesMap.put("620", "");
        valuesMap.put("732", "");
        valuesMap.put("852", "");
        valuesMap.put("9081", "1");

        // 2. 'data' 리스트의 첫 번째 객체 생성
        Map<String, Object> dataItem = new HashMap<>();
        dataItem.put("type", "0B");
        dataItem.put("name", "주식체결");
        dataItem.put("item", "005930");
        dataItem.put("values", valuesMap);

        // 3. 'data' List 생성
        List<Map<String, Object>> dataList = new ArrayList<>();
        dataList.add(dataItem);

        // 4. 최종 ROOT 객체 생성
        Map<String, Object> rootObject = new HashMap<>();
        rootObject.put("trnm", "REAL");
        rootObject.put("data", dataList);

        String jsonElement = gson.toJson(rootObject);

        return jsonElement;
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
                    String currentPrice = new BigDecimal(values.get("10").getAsString()).abs().toString();
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
                return itemObject.get("item").getAsString().replace("_AL", "");
            }
        }
        return null;
    }
}
