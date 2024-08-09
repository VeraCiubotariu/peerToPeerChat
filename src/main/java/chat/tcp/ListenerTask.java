package chat.tcp;

import chat.loggers.Loggers;
import chat.logic.Message;
import chat.utils.ChatUtils;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ListenerTask implements Runnable {
    private final Socket socket;

    public ListenerTask(Socket socket) {
        Loggers.infoLogger.info("Listening to: " + socket.getInetAddress().getHostAddress(), ListenerTask.class);
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            DataInputStream input = new DataInputStream(socket.getInputStream());

            while (socket.isConnected() && !Thread.currentThread().isInterrupted()) {
                if (socket.getInputStream().available() > 0) {
                    Loggers.infoLogger.info("Listener: {} is trying to read", Thread.currentThread().getName());
                    byte[] buf = new byte[65535];
                    input.read(buf);

                    String read = ChatUtils.data(buf).toString();
                    Loggers.infoLogger.info("Received: " + read, ListenerTask.class);

                    String[] parts = read.split("}");
                    for (String part : parts) {
                        String messageString = part + "}";
                        Message message = ChatUtils.getINSTANCE().getGson().fromJson(messageString, Message.class);

                        ChatUtils.getINSTANCE().addToMessageQueue(message);
                        System.out.println(message.getSender() + ": " + message.getMessage());

                        if (message.getMessage().startsWith("!update")) {
                            ChatUtils.getINSTANCE().getActiveGroup().updateConnections(message);
                        }
                    }


                }
            }
        } catch (IOException e) {
            Loggers.errorLogger.error("Error in ListenerTask", e);
            //throw new RuntimeException(e);
        }
    }
}
