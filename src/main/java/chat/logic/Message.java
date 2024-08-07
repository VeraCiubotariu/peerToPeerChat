package chat.logic;

import java.io.Serializable;

public class Message implements Serializable {
    private final String sender;
    private final String message;
    private final String receiver;
    private final String group;

    public Message(final String sender, final String message, final String receiver, final String group) {
        this.sender = sender;
        this.message = message;
        this.receiver = receiver;
        this.group = group;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sender='" + sender + '\'' +
                ", message='" + message + '\'' +
                ", receiver='" + receiver + '\'' +
                '}';
    }
}
