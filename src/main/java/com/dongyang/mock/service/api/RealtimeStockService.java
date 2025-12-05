package com.dongyang.mock.service.api;

import com.dongyang.mock.util.api.ApiConstants;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.net.URI;
import java.net.URISyntaxException;

@WebListener
public class RealtimeStockService implements ServletContextListener {
    public static final Gson gson = new Gson();

    public void connectSocket() {
        try{
            WebSocketService socket = new WebSocketService(new URI(ApiConstants.SOCKET_URL));
            socket.connectBlocking();

            JsonObject registerMessage = new JsonObject();
            registerMessage.addProperty("trnm", "REG"); // 서비스명
            registerMessage.addProperty("grp_no", "1"); // 그룹번호
            registerMessage.addProperty("refresh", "1"); // 기존등록유지여부

            JsonArray itemArray = new JsonArray();
            itemArray.add("039490"); // 실시간 등록 요소

            JsonArray typeArray = new JsonArray();
            typeArray.add("0B"); // 실시간 항목

            JsonObject dataObject = new JsonObject();
            dataObject.add("item", itemArray);
            dataObject.add("type", typeArray);

            registerMessage.add("data", gson.toJsonTree(new JsonObject[]{dataObject}));

            Thread.sleep(1000);

            socket.sendMessage(registerMessage);
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
