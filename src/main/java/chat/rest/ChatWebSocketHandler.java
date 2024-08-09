package chat.rest;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.CopyOnWriteArraySet;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        /*for (WebSocketSession wsSession : sessions) {
            if (wsSession.isOpen()) {
                wsSession.sendMessage(message);
            }
        }*/

        String payload = message.getPayload();
        System.out.println("Message received: " + payload);

        if (payload.startsWith("!hello")) {
            // Logic for handling a connection request
            System.out.println("Connection request: " + payload);
            session.sendMessage(new TextMessage("Welcome " + session.getId()));
        } else if (payload.startsWith("!bye")) {
            // Logic for handling a disconnection request
            System.out.println("Disconnection request: " + payload);
            session.sendMessage(new TextMessage("Goodbye " + session.getId()));
            session.close(CloseStatus.NORMAL);
        } else {
            // Broadcast message to all clients
            for (WebSocketSession wsSession : sessions) {
                if (wsSession.isOpen()) {
                    wsSession.sendMessage(new TextMessage(payload));  // Broadcast the message
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }
}
