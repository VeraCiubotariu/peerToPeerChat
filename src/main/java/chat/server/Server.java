package chat.server;

import chat.loggers.Loggers;
import chat.logic.Message;
import chat.logic.MessageWrapper;
import chat.logic.WorkerTask;
import chat.utils.ChatUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private final Set<String> pendingClients = new ConcurrentSkipListSet<>();
    private final Set<String> groupPendingClients = new ConcurrentSkipListSet<>();
    private final Map<String, List<String>> pendingGroupInvites = new ConcurrentHashMap<>();
    private final Map<String, InetAddress> incomingInvites = new ConcurrentHashMap<>();
    private final ExecutorService executors = Executors.newFixedThreadPool(10);
    private boolean running = true;


    public Server() throws RuntimeException {

    }

    public Map<String, InetAddress> getIncomingInvites() {
        return incomingInvites;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(ChatUtils.getINSTANCE().getPORT())) {
            while (running) {
                byte[] receive = new byte[65535];
                DatagramPacket receivePacket = new DatagramPacket(receive, receive.length);

                socket.receive(receivePacket);

                String receivedData = ChatUtils.data(receive).toString();

                Loggers.infoLogger.info("Data received: " + receivedData);

                Message message = ChatUtils.getINSTANCE().getGson().fromJson(receivedData, Message.class);
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

    public Set<String> getGroupPendingClients() {
        return groupPendingClients;
    }

    public Map<String, List<String>> getPendingGroupInvites() {
        return pendingGroupInvites;
    }
}
