package chat.tcp;

import chat.loggers.Loggers;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
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
            while (true) {
                DataInputStream input = new DataInputStream(socket.getInputStream());
                BufferedInputStream input2 = new BufferedInputStream(input);
                System.out.println(Arrays.toString(input2.readAllBytes()));
            }
        } catch (IOException e) {
            Loggers.errorLogger.error("Error in ListenerTask", e);
            //throw new RuntimeException(e);
        }
    }
}
