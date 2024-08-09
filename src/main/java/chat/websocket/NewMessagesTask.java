package chat.websocket;

import chat.loggers.Loggers;
import chat.logic.Message;
import chat.utils.ChatUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public class NewMessagesTask implements Runnable {
    private final WebSocketSession session;

    public NewMessagesTask(WebSocketSession session) {
        this.session = session;
    }

    @Override
    public void run() {
        while (session.isOpen()) {
            try {
                Message message = ChatUtils.getINSTANCE().getMessages().take();

                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message.getSender() + ": " + message.getMessage()));
                }

            } catch (InterruptedException | IOException e) {
                Loggers.errorLogger.error("Error in NewMessagesTask", e);
            }
        }
    }
}
