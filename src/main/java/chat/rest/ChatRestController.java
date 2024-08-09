package chat.rest;

import chat.logic.Message;
import chat.services.ChatService;
import chat.tcp.Group;
import chat.utils.Usefullstuff;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/chat")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ChatRestController {
    @Autowired
    private ChatService service;
    private final String myNickname = Usefullstuff.getINSTANCE().getNickname();

    @PostMapping("/connection-request/{nickname}")
    public ResponseEntity<String> requestConnection(@PathVariable String nickname) {
        Message message = new Message(myNickname, "!hello", nickname, null, null);
        service.sendDataUDP(message);
        return ResponseEntity.ok().body("OK");
    }

    @PostMapping("disconnection-request/{nickname}")
    public ResponseEntity<String> requestDisconnection(@PathVariable String nickname) {
        Message message = new Message(myNickname, "!bye", nickname, nickname, null);
        service.sendDataUDP(message);
        return ResponseEntity.ok().body("OK");
    }

    @PostMapping("/acknowledge-request/{nickname}")
    public ResponseEntity<String> requestAcknowledge(@PathVariable String nickname) {
        Message message = new Message(myNickname, "!ack", nickname, null, null);
        service.sendDataUDP(message);
        return ResponseEntity.ok().body("OK");
    }

    @GetMapping("/groups")
    public ResponseEntity<?> getAll() {
        Map<String, Group> groups = Usefullstuff.getINSTANCE().getConnectedGroups();
        return ResponseEntity.ok(groups.keySet());
    }

    @PostMapping("/groups/{id}")
    public ResponseEntity<String> addGroup(@PathVariable String id) {
        Group group = new Group(id, Usefullstuff.getINSTANCE().getServerSocket());
        Optional<Group> added = service.addGroup(group);

        if (added.isPresent()) {
            return ResponseEntity.ok().body("OK");
        }

        return ResponseEntity.ok().body("ERROR: Group already exists");
    }

    @PostMapping("/group-invitation-request/{groupID}/{receiver}")
    public ResponseEntity<String> requestGroupInvitation(@PathVariable String receiver, @PathVariable String groupID) {
        Message message = new Message(myNickname, "!invite", receiver, groupID, null);
        service.sendDataUDP(message);
        return ResponseEntity.ok().body("OK");
    }

    @PostMapping("/group-acknowledge-request/{groupID}/{receiver}")
    public ResponseEntity<String> requestGroupAcknowledge(@PathVariable String receiver, @PathVariable String groupID) {
        Message message = new Message(myNickname, "!ack", receiver, groupID, null);
        service.sendDataUDP(message);
        return ResponseEntity.ok().body("OK");
    }

    @GetMapping("/groups/active-group")
    public ResponseEntity<?> getActiveGroup() {
        Optional<Group> group = service.getActiveGroup();

        if (group.isPresent()) {
            return ResponseEntity.ok().body(group.get());
        }

        return ResponseEntity.ok().body("ERROR: You aren't in a group");
    }

    @PostMapping("/groups/active-group/{groupId}")
    public ResponseEntity<String> switchActiveGroup(@PathVariable String groupId) {
        Optional<Group> group = service.switchActiveGroup(groupId);

        if (group.isPresent()) {
            return ResponseEntity.ok().body("OK");
        }

        return ResponseEntity.ok().body("ERROR: Not connected to that group");
    }

    @PostMapping("/messages")
    public ResponseEntity<String> sendMessage(@RequestBody MessageDTO messageDTO) {
        Message message = new Message(myNickname, messageDTO.message(), messageDTO.receiver(), messageDTO.receiver(),
                null);

        Optional<Message> response = service.sendMessage(message);
        if (response.isPresent()) {
            return ResponseEntity.ok().body("OK");
        }

        return ResponseEntity.ok().body("ERROR: You aren't connected to that receiver");
    }

    @PostMapping("/groups/messages")
    public ResponseEntity<String> sendGroupMessage(@RequestBody GroupMessageDTO messageDTO) {
        Message message = new Message(myNickname, messageDTO.message(), null, messageDTO.group(), null);

        Optional<Message> response = service.sendMessage(message);
        if (response.isPresent()) {
            return ResponseEntity.ok().body("OK");
        }

        return ResponseEntity.ok().body("ERROR: You aren't connected to that group");
    }

    @PostMapping("/group-disconnection-request/{id}")
    public ResponseEntity<String> requestGroupDisconnection(@PathVariable String id) {
        Message message = new Message(myNickname, "!byeg", null, id, null);
        service.sendDataUDP(message);
        return ResponseEntity.ok().body("OK");
    }

    @PostMapping("/total-disconnection-request")
    public ResponseEntity<String> requestTotalDisconnection() {
        Message message = new Message(myNickname, "!byebye", null, null, null);
        service.sendDataUDP(message);
        return ResponseEntity.ok().body("OK");
    }
}
