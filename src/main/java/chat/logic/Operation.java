package chat.logic;

import chat.client.Client;
import chat.loggers.Loggers;
import chat.server.Server;
import chat.tcp.Group;
import chat.utils.ChatUtils;

import java.net.InetAddress;
import java.util.Map;
import java.util.Objects;

public enum Operation {
    CONNECTION("!hello", Operation::startConnection),
    GROUP_ACKNOWLEDGE("!ackg", Operation::groupAcknowledge),
    ACKNOWLEDGE("!ack", Operation::acknowledgeConnection),
    GROUP_BYE("!byeg", Operation::groupBye),
    CLOSE_CONNECTION("!bye", Operation::closeConnection),
    CLOSE_ALL_CONNECTIONS("!byebye", Operation::closeAllConnections),
    GROUP_INVITE("!invite", Operation::groupInvite);

    public final String regex;
    public final OperationLogic operation;

    Operation(final String regex, final OperationLogic op) {
        this.regex = regex;
        operation = op;
    }

    private static void closeAllConnections(final Server server, final MessageWrapper message) {
        for (Group group : ChatUtils.getINSTANCE().getConnectedGroups().values()) {
            group.closeConnections();
        }

        ChatUtils.getINSTANCE().setActiveGroup(null);
        server.shutdown();
        Client.shutdown();
    }

    private static void closeConnection(final Server server, final MessageWrapper message) {
        Group activeGroup = ChatUtils.getINSTANCE().getConnectedGroups().get(message.message().getGroup());

        if (activeGroup != null) {
            activeGroup.closeConnections();
            ChatUtils.getINSTANCE().getConnectedGroups().remove(activeGroup);
        }

        ChatUtils.getINSTANCE().setActiveGroup(null);
    }

    private static void startConnection(final Server server, final MessageWrapper message) {
        String sender = message.message().getSender();
        String receiver = message.message().getReceiver();
        String myNickname = ChatUtils.getINSTANCE().getNickname();
        if (Objects.equals(sender, myNickname)) {
            server.getPendingClients().add(receiver);
        } else if (Objects.equals(receiver, myNickname)) {
            if (message.message().getGroup() == null) {
                message.message().setGroup(sender);
            }

            server.getIncomingInvites().put(sender, message.senderIp());
            System.out.println("\n" + sender + ": " + message.message().getMessage());
        }
    }

    private static void acknowledgeConnection(final Server server, final MessageWrapper message) {
        String groupName = message.message().getGroup();
        String sender = message.message().getSender();
        String receiver = message.message().getReceiver();
        String mesajString = message.message().getMessage();
        Map<String, Group> connectedGroups = ChatUtils.getINSTANCE().getConnectedGroups();

        String myNickname = ChatUtils.getINSTANCE().getNickname();
        if (server.getPendingClients().contains(sender) && Objects.equals(myNickname, receiver)) {
            if (groupName == null) {
                message.message().setGroup(sender);
            }

            if (!connectedGroups.containsKey(groupName)) {
                Group group = new Group(groupName, ChatUtils.getINSTANCE().getServerSocket());
                connectedGroups.put(groupName, group);
                connectedGroups.get(groupName).addParticipant();
            }

            ChatUtils.getINSTANCE().setActiveGroup(connectedGroups.get(groupName));
            server.getPendingClients().remove(sender);

            System.out.println("\n" + sender + ": " + mesajString);
            ChatUtils.getINSTANCE().addToMessageQueue(message.message());
        } else if (Objects.equals(myNickname, sender)) {
            InetAddress receiverIp = server.getIncomingInvites().get(receiver);
            Loggers.infoLogger.info("Trying to connect to ip {}...", receiverIp);
            if (!connectedGroups.containsKey(groupName)) {
                Group group = new Group(groupName, ChatUtils.getINSTANCE().getServerSocket(), true);
                group.connectTo(receiverIp);
                connectedGroups.put(groupName, group);
            }

            ChatUtils.getINSTANCE().setActiveGroup(connectedGroups
                    .get(groupName));

            ChatUtils.getINSTANCE().addToMessageQueue(message.message());
            System.out.println("\n" + sender + ": " + mesajString);
        }
    }

    private static void groupBye(Server server, MessageWrapper messageWrapper) {
        String sender = messageWrapper.message().getSender();
        String myNickname = ChatUtils.getINSTANCE().getNickname();
        String group = messageWrapper.message().getGroup();
        Map<String, Group> connectedGroups = ChatUtils.getINSTANCE().getConnectedGroups();
        Group addresedGroup = connectedGroups.getOrDefault(group, null);

        if (sender.equals(myNickname) && addresedGroup != null) {
            Message byeMessage = new Message(myNickname, "!byeg", null,
                    addresedGroup.getName(), null);
            addresedGroup.sendMessage(byeMessage);
            closeConnection(server, messageWrapper);
        } else if(addresedGroup != null && connectedGroups.containsKey(group)){
            addresedGroup.removeSocket(messageWrapper.senderIp().getHostAddress());
        }

    }

    private static void groupAcknowledge(Server server, MessageWrapper messageWrapper) {
        String groupName = messageWrapper.message().getGroup();
        String sender = messageWrapper.message().getSender();
        String receiver = messageWrapper.message().getReceiver();
        String myNickname = ChatUtils.getINSTANCE().getNickname();
        Map<String, Group> connectedGroups = ChatUtils.getINSTANCE().getConnectedGroups();
        if (sender.equals(myNickname)) {
            if (!connectedGroups.containsKey(groupName)) {
                Group group = new Group(groupName, ChatUtils.getINSTANCE().getServerSocket(), true);
                connectedGroups.put(groupName, group);
            }
            ChatUtils.getINSTANCE().setActiveGroup(connectedGroups.get(groupName));

        } else if (receiver.equals(myNickname)) {
            if (server.getGroupPendingClients().contains(sender)) {
                if (connectedGroups.containsKey(groupName)) {
                    ChatUtils.getINSTANCE().setActiveGroup(connectedGroups.get(messageWrapper.message().getGroup()));
                    ChatUtils.getINSTANCE().getActiveGroup().newGroupMemberUpdate(messageWrapper.senderIp().getHostAddress());
                    server.getGroupPendingClients().remove(sender);
                }

                ChatUtils.getINSTANCE().addToMessageQueue(messageWrapper.message());
                System.out.println("\n" + sender + ": " + messageWrapper.message().getMessage());
            }
        }
    }

    private static void groupInvite(Server server, MessageWrapper messageWrapper) {
        String sender = messageWrapper.message().getSender();
        String receiver = messageWrapper.message().getReceiver();
        String myNickname = ChatUtils.getINSTANCE().getNickname();
        if (sender.equals(myNickname)) {
            server.getGroupPendingClients().add(receiver);
        } else if (receiver.equals(myNickname)) {
            ChatUtils.getINSTANCE().addToMessageQueue(messageWrapper.message());
            System.out.println("\n" + sender + ": " + messageWrapper.message().getMessage());
        }
    }


}
