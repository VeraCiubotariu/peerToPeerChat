package chat.tcp;

import chat.loggers.Loggers;
import chat.logic.Message;
import chat.utils.Usefullstuff;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

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

            while (socket.isConnected()) {
                if(socket.getInputStream().available() > 0) {
                    Loggers.infoLogger.info("Listener: {} is trying to read", Thread.currentThread().getName());
                    byte[] buf = new byte[65535];
                    input.read(buf);

                    String read = Usefullstuff.data(buf).toString();
                    Loggers.infoLogger.info("Received: " + read, ListenerTask.class);

                    Message message = Usefullstuff.getINSTANCE().getGson().fromJson(read, Message.class);
                    System.out.println("\n" + message.getSender() + ": " + message.getMessage());
                }
            }
        } catch (IOException e) {
            Loggers.errorLogger.error("Error in ListenerTask", e);
            //throw new RuntimeException(e);
        }
    }
}
