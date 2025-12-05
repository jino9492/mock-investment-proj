package com.dongyang.mock.service.api;

import com.dongyang.mock.service.auth.AuthService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class WebSocketService extends WebSocketClient {
    private final AuthService authService = new AuthService();
    String ACCESS_TOKEN = authService.issueAccessToken();
    private static final Gson gson = new Gson();

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
}
