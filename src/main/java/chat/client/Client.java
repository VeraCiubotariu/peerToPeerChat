package chat.client;

import chat.loggers.Loggers;
import chat.logic.Message;
import chat.tcp.Group;
import chat.utils.Usefullstuff;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Client implements Runnable {
    private static boolean running = true;
    private final Gson gson = new Gson();
    private final Scanner scanner = new Scanner(System.in);
    private DatagramSocket socket;
    private InetAddress address;
    private Message message;
    private String messageInput;
    private String to;
    private String group;

    public Client() {
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

    @Override
    public void run() {
        while (running) {
            readMessageFromConsole();
            message = new Message(Usefullstuff.getINSTANCE().getNickname(), messageInput, to, group, null);

            Loggers.infoLogger.info("Active group: " + Usefullstuff.getINSTANCE().getActiveGroup());

            if (Usefullstuff.getINSTANCE().getActiveGroup() == null) {
                sendDataUDP();
            } else {
                Usefullstuff.getINSTANCE().getActiveGroup().sendMessage(message);
            }
        }
    }

    private void readMessageFromConsole() {
        System.out.print("Message: ");
        messageInput = scanner.nextLine();
        if (messageInput.startsWith("!switch ")) {
            String newGroup = messageInput.split(" ")[1];
            Usefullstuff.getINSTANCE().setActiveGroup(Usefullstuff.getINSTANCE().getConnectedGroups().get(newGroup));
            readMessageFromConsole();
            return;
        }
        if (messageInput.startsWith("!toUDP")) {
            Usefullstuff.getINSTANCE().setActiveGroup(null);
            readMessageFromConsole();
            return;
        }
        if (messageInput.startsWith("!group")) {
            String groupName = messageInput.split(" ")[1];
            Group newGroup = new Group(groupName, Usefullstuff.getINSTANCE().getServerSocket());
            Usefullstuff.getINSTANCE().getConnectedGroups().put(groupName, newGroup);
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
                    = new DatagramPacket(buf, buf.length, address, Usefullstuff.getINSTANCE().getPORT());
            Loggers.infoLogger.info("Sending message :" + message.toString());
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
