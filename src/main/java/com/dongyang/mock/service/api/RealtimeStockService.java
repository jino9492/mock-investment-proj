package com.dongyang.mock.service.api;

import com.dongyang.mock.util.api.ApiConstants;
import com.dongyang.mock.util.api.InmemoryCacheManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@WebListener
public class RealtimeStockService implements ServletContextListener {
    public static final Gson gson = new Gson();
    private WebSocketService socket;

    public void connectSocket(List<String> stkCode, String refresh) {
        try {
            this.socket = new WebSocketService(new URI(ApiConstants.SOCKET_URL));
            this.socket.connectBlocking();

            JsonObject registerMessage = new JsonObject();
            registerMessage.addProperty("trnm", "REG"); // 서비스명
            registerMessage.addProperty("grp_no", "1"); // 그룹번호
            registerMessage.addProperty("refresh", refresh); // 기존등록유지여부

            JsonArray itemArray = new JsonArray();
            for(String item : stkCode) {
                itemArray.add(item); // 실시간 등록 요소
            }

            JsonArray typeArray = new JsonArray();
            typeArray.add("0B"); // 실시간 항목

            JsonObject dataObject = new JsonObject();
            dataObject.add("item", itemArray);
            dataObject.add("type", typeArray);

            registerMessage.add("data", gson.toJsonTree(new JsonObject[]{dataObject}));

            Thread.sleep(1000);

            this.socket.sendMessage(registerMessage);
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void disconnectSocket() {
        if (this.socket != null && this.socket.isOpen()) {
            try {
                JsonObject unregisterMessage = new JsonObject();
                unregisterMessage.addProperty("trnm", "UNREG");
                this.socket.sendMessage(unregisterMessage);

                this.socket.closeBlocking();
                System.out.println("웹소켓 연결이 안전하게 종료되었습니다.");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("웹소켓 연결 종료 중 인터럽트 발생: " + e.getMessage());
            }
        } else if (this.socket != null) {
            System.out.println("웹소켓 연결이 이미 닫혀 있거나 초기화되지 않았습니다.");
        }
        this.socket = null;
    }

    public boolean isSocketOpen() {
        return this.socket != null && this.socket.isOpen();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        disconnectSocket();
        System.out.println("RealtimeStockService 리스너 종료. 웹소켓 연결 해제 완료.");
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
    }
}