package chat.tcp;

import chat.loggers.Loggers;
import chat.logic.Message;
import chat.utils.ChatUtils;
import com.google.gson.Gson;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Group {
    private final Set<Socket> participants = new HashSet<>();
    private final ServerSocket myServer;
    private final Gson gson = new Gson();
    private final String name;
    private boolean newlyConnected = false;
    private ExecutorService executor = null;
    private Thread accepter;
    private Object lock = new Object();

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
            synchronized (lock) {
                participants.add(socket);
            }
        } catch (IOException e) {
            Loggers.errorLogger.error(e.getClass() + " :" + e.getMessage());
        }
    }

    public void sendMessage(Message message) {
        for (Socket socket : participants) {
            try {
                Loggers.infoLogger.info("Sending message {} to {}", message.getMessage(), socket.getInetAddress().getAddress());
                DataOutputStream stream = new DataOutputStream(socket.getOutputStream());
                String output = gson.toJson(message, Message.class);
                byte[] buf = output.getBytes();
                stream.write(buf);
                Loggers.infoLogger.info("Message sent to {}!", socket.getInetAddress().getAddress());
            } catch (IOException e) {
                Loggers.errorLogger.error(e.getClass() + " :" + e.getMessage());
                //throw new RuntimeException(e);
            }
        }
    }

    public void connectTo(InetAddress ip) {
        try {
            Loggers.infoLogger.info("Connecting to {} of group {}", ip.getHostAddress(), name);
            Socket socket = new Socket(ip.getHostAddress(), ChatUtils.getINSTANCE().getPORT());
            Loggers.infoLogger.info("Connected to {} of group {}", ip.getHostAddress(), name);

            synchronized (lock) {
                participants.add(socket);
            }
        } catch (IOException e) {
            Loggers.errorLogger.error(e.getClass() + " :" + e.getMessage());
            //throw new RuntimeException(e);
        }
    }

    public void connectTo(String ip) {
        try {
            Loggers.infoLogger.info("Connecting to {} of group {}", ip, name);
            Socket socket = new Socket(ip, ChatUtils.getINSTANCE().getPORT());
            Loggers.infoLogger.info("Connected to {} of group {}", ip, name);

            synchronized (lock) {
                participants.add(socket);
            }
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
        executor = null;
    }

    public void startServerSocketTask() {
        Loggers.infoLogger.info("Starting server socket task {}", Thread.currentThread().getName());
        accepter = new Thread(new AccepterTask(this));
        accepter.start();
    }

    public void stopAccepter() {
        if (accepter != null) {
            Loggers.infoLogger.info("Stopping server socket task {}", Thread.currentThread().getName());
            accepter.interrupt();
            accepter = null;
        }
    }

    public void closeConnections() {
        for (Socket socket : participants) {
            try {
                socket.close();
            } catch (IOException e) {
                Loggers.errorLogger.error(e.getClass() + " :" + e.getMessage());
            }
        }
    }

    public void newGroupMemberUpdate(String newMemberIp) {
        Loggers.infoLogger.info("New group member update for {}", newMemberIp);
        connectTo(newMemberIp);

        Message updateMessage = new Message(ChatUtils.getINSTANCE().getNickname(), "!update", null,
                name, getGroupMembersIps());

        sendMessage(updateMessage);

        this.stopListeners();
        this.startListeners();
    }

    public synchronized void updateConnections(Message updateMessage) {
        Loggers.infoLogger.info("Updating connections for {}", name);
        List<String> myIps = getGroupMembersIps();

        for (String ip : updateMessage.getIps()) {
            try {
                if (!myIps.contains(ip) && !Objects.equals(ip, InetAddress.getLocalHost().getHostAddress())) {
                    connectTo(ip);
                }
            } catch (UnknownHostException e) {
                Loggers.errorLogger.error("UnknownHost cika");
            }
        }
        this.stopListeners();
        this.startListeners();
    }

    public List<String> getGroupMembersIps() {
        List<String> ips = new ArrayList<>();

        for (Socket socket : participants) {
            ips.add(socket.getInetAddress().getHostAddress());
        }

        return ips;
    }
    public void removeSocket(String ip){
        for(Socket socket : participants){
            if(socket.getInetAddress().getHostAddress() == ip){
                participants.remove(socket);
            }
        }
    }
}
