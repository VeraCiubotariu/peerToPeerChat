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
    ACKNOWLEDGE("!ack", Operation::acknowledgeConnection),
    CLOSE_CONNECTION("!bye", Operation::closeConnection),
    CLOSE_ALL_CONNECTIONS("!byebye", Operation::closeAllConnections);

    public final String regex;
    public final OperationLogic operation;

    Operation(final String regex, final OperationLogic op) {
        this.regex = regex;
        operation = op;
    }

    private static void closeAllConnections(final Server server, final MessageWrapper message) {
        server.shutdown();
        Client.shutdown();
    }

    private static void closeConnection(final Server server, final MessageWrapper message) {
        if(Usefullstuff.getINSTANCE().getActiveGroup() != null){
            Usefullstuff.getINSTANCE().getActiveGroup().closeConnections();
            Usefullstuff.getINSTANCE().getConnectedGroups().remove(Usefullstuff.getINSTANCE().getActiveGroup());
        }

        Usefullstuff.getINSTANCE().setActiveGroup(null);
    }

    private static void startConnection(final Server server, final MessageWrapper message) {
        if (Objects.equals(message.message().getSender(), Usefullstuff.getINSTANCE().getNickname())) {
            server.getPendingClients().add(message.message().getReceiver());
        } else if (Objects.equals(message.message().getReceiver(), Usefullstuff.getINSTANCE().getNickname())) {
            server.getIncomingInvites().put(message.message().getSender(), message.senderIp());
            System.out.println("\n" + message.message().getSender() + ": " + message.message().getMessage());
        }
    }

    private static void acknowledgeConnection(final Server server, final MessageWrapper message) {
        String groupName = message.message().getGroup();
        if (server.getPendingClients().contains(message.message().getSender()) && Objects.equals(Usefullstuff.getINSTANCE().getNickname(), message.message().getReceiver())) {
            if (!Usefullstuff.getINSTANCE().getConnectedGroups().containsKey(groupName)) {
                Group group = new Group(groupName, server.getServerSocket());
                Usefullstuff.getINSTANCE().getConnectedGroups().put(groupName, group);
            }

            Usefullstuff.getINSTANCE().getConnectedGroups().get(groupName).addParticipant();
            Usefullstuff.getINSTANCE().setActiveGroup(Usefullstuff.getINSTANCE().getConnectedGroups().get(groupName));
            server.getPendingClients().remove(message.message().getSender());

            System.out.println("\n" + message.message().getSender() + ": " + message.message().getMessage());
        } else if (Objects.equals(Usefullstuff.getINSTANCE().getNickname(), message.message().getSender())) {
            //TODO: Nu ar trebui sa verificam daca grupul s-a alaturat deja?
            InetAddress receiverIp = server.getIncomingInvites().get(message.message().getReceiver());
            Loggers.infoLogger.info("Trying to connect to ip {}...", receiverIp);
            Group group = new Group(groupName, server.getServerSocket(), true);
            group.connectTo(receiverIp);
            Usefullstuff.getINSTANCE().setActiveGroup(group);
            Usefullstuff.getINSTANCE().getConnectedGroups().put(groupName, group);

            System.out.println("\n" + message.message().getSender() + ": " + message.message().getMessage());
        }
    }
}
