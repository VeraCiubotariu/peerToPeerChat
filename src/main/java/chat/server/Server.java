package chat.server;

import chat.loggers.Loggers;
import chat.logic.Message;
import chat.logic.MessageWrapper;
import chat.logic.WorkerTask;
import chat.utils.Usefullstuff;

import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private final Set<String> pendingClients = new ConcurrentSkipListSet<>();
    private final Map<String, InetAddress> incomingInvites = new ConcurrentHashMap<>();
    private final ExecutorService executors = Executors.newFixedThreadPool(10);
    private final int port = 7401;
    private boolean running = true;
    private ServerSocket serverSocket;

    public Server() throws RuntimeException {
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            Loggers.errorLogger.error(ex.getMessage());
        }
    }

    public Map<String, InetAddress> getIncomingInvites() {
        return incomingInvites;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            while (running) {
                byte[] receive = new byte[65535];
                DatagramPacket receivePacket = new DatagramPacket(receive, receive.length);

                socket.receive(receivePacket);

                String receivedData = Usefullstuff.data(receive).toString();

                Loggers.infoLogger.info("Data received: " + receivedData);

                Message message = Usefullstuff.getINSTANCE().getGson().fromJson(receivedData, Message.class);
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

    public Set<String> getPendingClients() {
        return pendingClients;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }
}
