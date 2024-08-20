package chat.websocket;

import chat.loggers.Loggers;
import chat.logic.Message;
import chat.utils.ChatUtils;
import com.google.gson.Gson;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public class NewMessagesTask implements Runnable {
    private final WebSocketSession session;
    private final Gson gson = new Gson();

    public NewMessagesTask(WebSocketSession session) {
        this.session = session;
    }

    @Override
    public void run() {
        while (session.isOpen()) {
            try {
                Message message = ChatUtils.getINSTANCE().getMessages().take();

                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(gson.toJson(message)));
                }

            } catch (InterruptedException | IOException e) {
                Loggers.errorLogger.error("Error in NewMessagesTask", e);
            }
        }
    }
}
