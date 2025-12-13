package com.dongyang.mock.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@ServerEndpoint("/ws/realtime")
public class RealtimeWebSocket {

    private static final Logger LOGGER = Logger.getLogger(RealtimeWebSocket.class.getName());

    private static final Set<Session> allSessions = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) {
        allSessions.add(session);
        LOGGER.info("Client connected: " + session.getId() + ". Total sessions: " + allSessions.size());
    }

    @OnClose
    public void onClose(Session session) {
        allSessions.remove(session);
        LOGGER.info("Client disconnected: " + session.getId() + ". Total sessions: " + allSessions.size());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.warning("WebSocket Error on session " + session.getId() + ": " + throwable.getMessage());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        LOGGER.info("Received message from client " + session.getId() + ": " + message);
    }

    public static void broadcast(String message) {
        for (Session session : allSessions) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    LOGGER.severe("Error sending message to session " + session.getId() + ": " + e.getMessage());
                }
            }
        }
    }
}