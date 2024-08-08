package chat.rest;

import chat.client.Client;
import chat.loggers.Loggers;
import chat.logic.Message;
import chat.logic.MessageWrapper;
import chat.server.Server;
import chat.service.ChatService;
import chat.tcp.Group;
import chat.utils.Usefullstuff;
import com.google.gson.Gson;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class RestService {
    //private final Server server;
    private final Gson gson = new Gson();
    private DatagramSocket socket;
    private InetAddress address;

    public RestService() {
        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName("10.4.1.255");
        } catch (SocketException | UnknownHostException e) {
            Loggers.errorLogger.error(e.getClass() + " :" + e.getMessage());
        }
    }

    public void sendDataUDP(Message message) {
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

    public Optional<Group> addGroup(Group group){
        Map<String, Group> connectedGroups = Usefullstuff.getINSTANCE().getConnectedGroups();
        if (connectedGroups.containsKey(group.getName())) {
            return Optional.empty();
        }

        connectedGroups.put(group.getName(), group);
        return Optional.of(group);
    }

    public Optional<Group> getActiveGroup(){
        if(Usefullstuff.getINSTANCE().getActiveGroup() != null) {
            return Optional.of(Usefullstuff.getINSTANCE().getActiveGroup());
        }
        return Optional.empty();
    }

    public Optional<Group> switchActiveGroup(String groupName){
        Map<String, Group> connectedGroups = Usefullstuff.getINSTANCE().getConnectedGroups();
        if (connectedGroups.containsKey(groupName)) {
            Group group = connectedGroups.get(groupName);
            Usefullstuff.getINSTANCE().setActiveGroup(group);
            return Optional.of(group);
        }
        return Optional.empty();
    }
}
