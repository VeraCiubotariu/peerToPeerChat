package chat.logic;

import chat.client.Client;
import chat.loggers.Loggers;
import chat.server.Server;
import chat.tcp.Group;
import chat.utils.Usefullstuff;

import java.net.InetAddress;
import java.util.Objects;

public enum Operation {
    CONNECTION("!hello", Operation::startConnection),
    GROUP_ACKNOWLEDGE("!ackg", Operation::groupAcknowledge),
    ACKNOWLEDGE("!ack", Operation::acknowledgeConnection),
    CLOSE_CONNECTION("!bye", Operation::closeConnection),
    CLOSE_ALL_CONNECTIONS("!byebye", Operation::closeAllConnections),
    GROUP_INVITE("!invite", Operation::groupInvite),
    GROUP_BYE("!byeg", Operation::groupBye);

    public final String regex;
    public final OperationLogic operation;

    Operation(final String regex, final OperationLogic op) {
        this.regex = regex;
        operation = op;
    }

    private static void closeAllConnections(final Server server, final MessageWrapper message) {
        for (Group group : Usefullstuff.getINSTANCE().getConnectedGroups().values()) {
            group.closeConnections();
        }

        Usefullstuff.getINSTANCE().setActiveGroup(null);
        server.shutdown();
        Client.shutdown();
    }

    private static void closeConnection(final Server server, final MessageWrapper message) {
        if (Usefullstuff.getINSTANCE().getActiveGroup() != null) {
            Usefullstuff.getINSTANCE().getActiveGroup().closeConnections();
            Usefullstuff.getINSTANCE().getConnectedGroups().remove(Usefullstuff.getINSTANCE().getActiveGroup());
        }

        Usefullstuff.getINSTANCE().setActiveGroup(null);
    }

    private static void startConnection(final Server server, final MessageWrapper message) {
        if (Objects.equals(message.message().getSender(), Usefullstuff.getINSTANCE().getNickname())) {
            server.getPendingClients().add(message.message().getReceiver());
        } else if (Objects.equals(message.message().getReceiver(), Usefullstuff.getINSTANCE().getNickname())) {
            if (message.message().getGroup() == null) {
                message.message().setGroup(message.message().getSender());
            }

            server.getIncomingInvites().put(message.message().getSender(), message.senderIp());
            System.out.println("\n" + message.message().getSender() + ": " + message.message().getMessage());
        }
    }

    private static void acknowledgeConnection(final Server server, final MessageWrapper message) {
        String groupName = message.message().getGroup();
        if (server.getPendingClients().contains(message.message().getSender()) && Objects.equals(Usefullstuff.getINSTANCE().getNickname(), message.message().getReceiver())) {
            if (message.message().getGroup() == null) {
                message.message().setGroup(message.message().getSender());
            }

            if (!Usefullstuff.getINSTANCE().getConnectedGroups().containsKey(groupName)) {
                Group group = new Group(groupName, Usefullstuff.getINSTANCE().getServerSocket());
                Usefullstuff.getINSTANCE().getConnectedGroups().put(groupName, group);
                Usefullstuff.getINSTANCE().getConnectedGroups().get(groupName).addParticipant();
            }

            Usefullstuff.getINSTANCE().setActiveGroup(Usefullstuff.getINSTANCE().getConnectedGroups().get(groupName));
            server.getPendingClients().remove(message.message().getSender());

            System.out.println("\n" + message.message().getSender() + ": " + message.message().getMessage());
        } else if (Objects.equals(Usefullstuff.getINSTANCE().getNickname(), message.message().getSender())) {
            InetAddress receiverIp = server.getIncomingInvites().get(message.message().getReceiver());
            Loggers.infoLogger.info("Trying to connect to ip {}...", receiverIp);
            if (!Usefullstuff.getINSTANCE().getConnectedGroups().containsKey(groupName)) {
                Group group = new Group(groupName, Usefullstuff.getINSTANCE().getServerSocket(), true);
                group.connectTo(receiverIp);
                Usefullstuff.getINSTANCE().getConnectedGroups().put(groupName, group);
            }

            Usefullstuff.getINSTANCE().setActiveGroup(Usefullstuff.getINSTANCE().getConnectedGroups()
                    .get(message.message().getGroup()));

            System.out.println("\n" + message.message().getSender() + ": " + message.message().getMessage());
        }
    }

    private static void groupBye(Server server, MessageWrapper messageWrapper) {
        Message byeMessage = new Message(Usefullstuff.getINSTANCE().getNickname(), "!byeg", null,
                Usefullstuff.getINSTANCE().getActiveGroup().getName(), null);
        Usefullstuff.getINSTANCE().getActiveGroup().sendMessage(byeMessage);
        closeConnection(server, messageWrapper);
    }

    private static void groupAcknowledge(Server server, MessageWrapper messageWrapper) {
        if (messageWrapper.message().getSender().equals(Usefullstuff.getINSTANCE().getNickname())) {
            String groupName = messageWrapper.message().getGroup();

            if (!Usefullstuff.getINSTANCE().getConnectedGroups().containsKey(groupName)) {
                Group group = new Group(groupName, Usefullstuff.getINSTANCE().getServerSocket(), true);
                group.startServerSocketTask();
                Usefullstuff.getINSTANCE().getConnectedGroups().put(groupName, group);
            }

            Usefullstuff.getINSTANCE().setActiveGroup(Usefullstuff.getINSTANCE().getConnectedGroups()
                    .get(messageWrapper.message().getGroup()));

            System.out.println("\n" + messageWrapper.message().getSender() + ": " + messageWrapper.message().getMessage());
        } else if (messageWrapper.message().getReceiver().equals(Usefullstuff.getINSTANCE().getNickname())) {
            if (server.getGroupPendingClients().contains(messageWrapper.message().getSender())) {
                Usefullstuff.getINSTANCE().getActiveGroup().newGroupMemberUpdate(messageWrapper.senderIp().getHostAddress());
                server.getGroupPendingClients().remove(messageWrapper.message().getSender());
            }
        }
    }

    private static void groupInvite(Server server, MessageWrapper messageWrapper) {
        if (messageWrapper.message().getSender().equals(Usefullstuff.getINSTANCE().getNickname())) {
            server.getGroupPendingClients().add(messageWrapper.message().getReceiver());
        } else if (messageWrapper.message().getReceiver().equals(Usefullstuff.getINSTANCE().getNickname())) {
            System.out.println("\n" + messageWrapper.message().getSender() + ": "
                    + messageWrapper.message().getMessage());
        }
    }
}
