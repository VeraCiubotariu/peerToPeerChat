package chat.tcp;

import chat.loggers.Loggers;
import chat.logic.Message;
import com.google.gson.Gson;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Group {
    private final Set<Socket> participants = new HashSet<>();
    private final ServerSocket myServer;
    private final Gson gson = new Gson();
    private boolean newlyConnected = false;

    private final String name;
    private ExecutorService executor = null;
    private static final int PORT = 7401;

    public Group(String name, ServerSocket server) {
        Loggers.infoLogger.info("Group " + name + " created");
        this.myServer = server;
        this.name = name;
    }

    public Group(String name, ServerSocket serverSocket, boolean newlyConnected) {
        Loggers.infoLogger.info("Group " + name + " created");
        this.newlyConnected = newlyConnected;
        this.name = name;
        this.myServer = serverSocket;
    }

    public void addParticipant() {
        try {
            Loggers.infoLogger.info("Accepting participant in group {}", name);
            Socket socket = myServer.accept();
            Loggers.infoLogger.info("Accepted participant {} in group {}", socket.getInetAddress().getAddress(), name);
            participants.add(socket);
        } catch (IOException e) {
            Loggers.errorLogger.error(e.getClass() + " :" + e.getMessage());
        }
    }

    public void sendMessage(Message message) {
        for (Socket socket : participants) {
            try {
                DataOutputStream stream = new DataOutputStream(socket.getOutputStream());
                String output = gson.toJson(message, Message.class);
                byte[] buf = output.getBytes();
                stream.write(buf);

            } catch (IOException e) {
                Loggers.errorLogger.error(e.getClass() + " :" + e.getMessage());
                //throw new RuntimeException(e);
            }
        }
    }

    public void connectTo(InetAddress ip) {
        try {
            Loggers.infoLogger.info("Connecting to {} of group {}", ip.getHostAddress(), name);
            Socket socket = new Socket(ip.getHostAddress(), PORT);
            Loggers.infoLogger.info("Connected to {} of group {}", ip.getHostAddress(), name);
            participants.add(socket);
        } catch (IOException e) {
            Loggers.errorLogger.error(e.getClass() + " :" + e.getMessage());
            //throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    public void startListeners() {
        if (executor != null) {
            return;
        }

        executor = Executors.newCachedThreadPool();

        for (Socket socket : participants) {
            executor.submit(new ListenerTask(socket));
        }
    }

    public void stopListeners() {
        if (executor == null) {
            return;
        }

        executor.shutdownNow();
    }
}
