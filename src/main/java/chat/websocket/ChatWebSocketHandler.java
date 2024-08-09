package chat.websocket;

import chat.logic.Message;
import chat.services.ChatService;
import chat.tcp.Group;
import chat.utils.ChatUtils;
import com.google.gson.Gson;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.CopyOnWriteArraySet;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final Gson gson = new Gson();
    private final ChatService service = new ChatService();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        Thread listenerThread = new Thread(new NewMessagesTask(session));
        listenerThread.start();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("Data received: " + payload);
        Message msg = gson.fromJson(payload, Message.class);

        String messageInput = msg.getMessage();

        if (messageInput.startsWith("!switch ")) {
            String newGroup = messageInput.split(" ")[1];
            ChatUtils.getINSTANCE().setActiveGroup(ChatUtils.getINSTANCE().getConnectedGroups().get(newGroup));
            return;
        }
        if (messageInput.startsWith("!toUDP")) {
            ChatUtils.getINSTANCE().setActiveGroup(null);
            return;
        }
        if (messageInput.startsWith("!group")) {
            String groupName = messageInput.split(" ")[1];
            Group newGroup = new Group(groupName, ChatUtils.getINSTANCE().getServerSocket());
            ChatUtils.getINSTANCE().getConnectedGroups().put(groupName, newGroup);
            return;
        }

        if (ChatUtils.getINSTANCE().getActiveGroup() == null) {
            service.sendDataUDP(msg);
        } else {
            ChatUtils.getINSTANCE().getActiveGroup().sendMessage(msg);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }
}
