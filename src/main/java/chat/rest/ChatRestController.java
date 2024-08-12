package chat.rest;

import chat.logic.Message;
import chat.services.ChatService;
import chat.tcp.Group;
import chat.utils.ChatUtils;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private final String myNickname = ChatUtils.getINSTANCE().getNickname();

    @PostMapping("/connection-request/{nickname}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message format correct",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "An error occurred while sending",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))
    })
    public ResponseEntity<String> requestConnection(@PathVariable String nickname) {
        Message message = new Message(myNickname, "!hello", nickname, null, null);
        String returnedStatus = service.sendDataUDP(message);
        return new ResponseEntity<>(
                returnedStatus,
                returnedStatus.equals("Failed to send message") ? HttpStatus.BAD_REQUEST : HttpStatus.OK
        );
    }

    @PostMapping("disconnection-request/{nickname}")
    public ResponseEntity<String> requestDisconnection(@PathVariable String nickname) {
        Message message = new Message(myNickname, "!bye", nickname, nickname, null);
        String returnedStatus = service.sendDataUDP(message);
        return new ResponseEntity<>(
                returnedStatus,
                returnedStatus.equals("Failed to send message") ? HttpStatus.BAD_REQUEST : HttpStatus.OK
        );
    }

    @PostMapping("/acknowledge-request/{nickname}")
    public ResponseEntity<String> requestAcknowledge(@PathVariable String nickname) {
        Message message = new Message(myNickname, "!ack", nickname, null, null);
        String returnedStatus = service.sendDataUDP(message);
        return new ResponseEntity<>(
                returnedStatus,
                returnedStatus.equals("Failed to send message") ? HttpStatus.BAD_REQUEST : HttpStatus.OK
        );
    }

    @GetMapping("/groups")
    public ResponseEntity<?> getAll() {
        Map<String, Group> groups = ChatUtils.getINSTANCE().getConnectedGroups();
        return new ResponseEntity<>(
                groups.keySet(),
                HttpStatus.OK
        );
    }

    @PostMapping("/groups/{id}")
    public ResponseEntity<String> addGroup(@PathVariable String id) {
        Group group = new Group(id, ChatUtils.getINSTANCE().getServerSocket());
        Optional<Group> added = service.addGroup(group);

        if (added.isPresent()) {
            return new ResponseEntity<>(
                    "Group created",
                    HttpStatus.OK
            );
        }
        return new ResponseEntity<>(
                "Group already exists",
                HttpStatus.BAD_REQUEST
        );
    }

    @PostMapping("/group-invitation-request/{groupID}/{receiver}")
    public ResponseEntity<String> requestGroupInvitation(@PathVariable String receiver, @PathVariable String groupID) {
        Message message = new Message(myNickname, "!invite", receiver, groupID, null);
        String returnedStatus = service.sendDataUDP(message);
        return new ResponseEntity<>(
                returnedStatus,
                returnedStatus.equals("Failed to send message") ? HttpStatus.BAD_REQUEST : HttpStatus.OK
        );
    }

    @PostMapping("/group-acknowledge-request/{groupID}/{receiver}")
    public ResponseEntity<String> requestGroupAcknowledge(@PathVariable String receiver, @PathVariable String groupID) {
        Message message = new Message(myNickname, "!ack", receiver, groupID, null);
        String returnedStatus = service.sendDataUDP(message);
        return new ResponseEntity<>(
                returnedStatus,
                returnedStatus.equals("Failed to send message") ? HttpStatus.BAD_REQUEST : HttpStatus.OK
        );
    }

    @GetMapping("/groups/active-group")
    public ResponseEntity<?> getActiveGroup() {
        Optional<Group> group = service.getActiveGroup();

        if (group.isPresent()) {
            return new ResponseEntity<>(
                    group.get(),
                    HttpStatus.OK
            );
        }
        return new ResponseEntity<>(
                "You aren't connected to a group",
                HttpStatus.BAD_REQUEST
        );
    }

    @PostMapping("/groups/active-group/{groupId}")
    public ResponseEntity<String> switchActiveGroup(@PathVariable String groupId) {
        Optional<Group> group = service.switchActiveGroup(groupId);

        if (group.isPresent()) {
            return new ResponseEntity<>(
                    "Group switched",
                    HttpStatus.OK
            );
        }
        return new ResponseEntity<>(
                "You aren't connected to that group",
                HttpStatus.BAD_REQUEST
        );
    }

    @PostMapping("/messages")
    public ResponseEntity<String> sendMessage(@RequestBody MessageDTO messageDTO) {
        Message message = new Message(myNickname, messageDTO.message(), messageDTO.receiver(), messageDTO.receiver(),
                null);

        Optional<Message> response = service.sendMessage(message);
        if (response.isPresent()) {
            return new ResponseEntity<>(
                    "Message sent",
                    HttpStatus.OK
            );
        }
        return new ResponseEntity<>(
                "You aren't connected to that person(receiver)",
                HttpStatus.BAD_REQUEST
        );
    }

    @PostMapping("/groups/messages")
    public ResponseEntity<String> sendGroupMessage(@RequestBody GroupMessageDTO messageDTO) {
        Message message = new Message(myNickname, messageDTO.message(), null, messageDTO.group(), null);

        Optional<Message> response = service.sendMessage(message);
        if (response.isPresent()) {
            return new ResponseEntity<>(
                    "Message sent",
                    HttpStatus.OK
            );
        }
        return new ResponseEntity<>(
                "You aren't connected to that group",
                HttpStatus.BAD_REQUEST
        );
    }

    @PostMapping("/group-disconnection-request/{id}")
    public ResponseEntity<String> requestGroupDisconnection(@PathVariable String id) {
        Message message = new Message(myNickname, "!byeg", null, id, null);
        String returnedStatus = service.sendDataUDP(message);
        return new ResponseEntity<>(
                returnedStatus,
                returnedStatus.equals("Failed to send message") ? HttpStatus.BAD_REQUEST : HttpStatus.OK
        );
    }

    @PostMapping("/total-disconnection-request")
    public ResponseEntity<String> requestTotalDisconnection() {
        Message message = new Message(myNickname, "!byebye", null, null, null);
        String returnedStatus = service.sendDataUDP(message);
        return new ResponseEntity<>(
                returnedStatus,
                returnedStatus.equals("Failed to send message") ? HttpStatus.BAD_REQUEST : HttpStatus.OK
        );
    }
}
