package chat.server;

import chat.loggers.Loggers;
import chat.logic.Message;
import chat.logic.MessageWrapper;
import chat.logic.WorkerTask;
import chat.tcp.Group;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private final Map<String, Group> connectedGroups = new ConcurrentHashMap<>();
    private final Set<String> pendingClients = new ConcurrentSkipListSet<>();
    private final Map<String, InetAddress> incomingInvites = new ConcurrentHashMap<>();
    private final ExecutorService executors = Executors.newFixedThreadPool(10);
    private final Gson gson = new Gson();
    private final String nickname;
    private final int port = 7401;
    private boolean running = true;
    private ServerSocket serverSocket;

    public Server(final String nickname) throws RuntimeException {
        this.nickname = nickname;

        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            Loggers.errorLogger.error(ex.getMessage());
        }
    }

    public Map<String, InetAddress> getIncomingInvites() {
        return incomingInvites;
    }

    public static StringBuilder data(final byte[] a) {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0) {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            while (running) {
                byte[] receive = new byte[65535];
                DatagramPacket receivePacket = new DatagramPacket(receive, receive.length);

                socket.receive(receivePacket);

                String receivedData = data(receive).toString();

                Loggers.infoLogger.info("Data received: " + receivedData);

                Message message = gson.fromJson(receivedData, Message.class);
                Loggers.infoLogger.info("Message received :" + message.toString());

                MessageWrapper wrapper = new MessageWrapper(message, receivePacket.getAddress());

                executors.submit(new WorkerTask(this, wrapper));
            }
        } catch (SocketException e) {
            Loggers.errorLogger.error("SocketException :" + e.getMessage());
        } catch (IOException e) {
            Loggers.errorLogger.error("IOException :" + e.getMessage());
        }
    }

    public void shutdown() {
        running = false;
    }

    public String getNickname() {
        return nickname;
    }

    public Set<String> getPendingClients() {
        return pendingClients;
    }

    public Map<String, Group> getConnectedGroups() {
        return connectedGroups;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }
}
