package chat.client;

import chat.loggers.Loggers;
import chat.logic.Message;
import chat.tcp.Group;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Client implements Runnable {
    private static boolean running = true;
    private static Group activeGroup = null;
    private final String nickname;
    private final int port = 7401;
    private final Gson gson = new Gson();
    private final Scanner scanner = new Scanner(System.in);
    private DatagramSocket socket;
    private InetAddress address;
    private Message message;
    private String messageInput;
    private String to;
    private String group;

    public Client(final String nickname) {
        this.nickname = nickname;
        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName("10.4.1.255");
        } catch (SocketException | UnknownHostException e) {
            Loggers.errorLogger.error(e.getClass() + " :" + e.getMessage());
        }
    }

    public static void shutdown() {
        running = false;
    }

    public static Group getActiveGroup() {
        return activeGroup;
    }

    public static void setActiveGroup(Group activeGroup) {
        activeGroup.stopListeners();
        Client.activeGroup = activeGroup;
        activeGroup.startListeners();

        Loggers.infoLogger.info("Changed active group: " + activeGroup.getName());
    }

    @Override
    public void run() {
        while (running) {
            readMessageFromConsole();
            message = new Message(nickname, messageInput, to, group);

            if (activeGroup == null) {
                sendDataUDP();
            } else {
                activeGroup.sendMessage(message);
            }
        }
    }

    private void readMessageFromConsole() {
        System.out.print("Message: ");
        messageInput = scanner.nextLine();
        /*if(messageInput.startsWith("!switch ")){
            String newGroup = messageInput.split(" ")[1];
            activeGroup = server.getConnectedGroups().get(newGroup);
            readMessageFromConsole();
            return;
        }*/
        if (messageInput.startsWith("!toUDP")) {
            activeGroup = null;
            readMessageFromConsole();
            return;
        }
        System.out.print("To: ");
        to = scanner.nextLine();

        System.out.print("Group: ");
        group = scanner.nextLine();
        if (group.equals("null")) {
            group = null;
        }
    }

    private void sendDataUDP() {
        try {
            String output = gson.toJson(message, Message.class);
            byte[] buf = output.getBytes();
            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length, address, port);
            Loggers.infoLogger.info("Sending message :" + message.toString());
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
