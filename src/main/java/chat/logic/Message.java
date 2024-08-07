package chat.logic;

import java.util.List;

public class Message {
    private final String sender;
    private final String message;
    private final String receiver;
    private final List<String> ips;
    private String group;

    public Message(final String sender, final String message, final String receiver, final String group, final List<String> ips) {
        this.sender = sender;
        this.message = message;
        this.receiver = receiver;
        this.group = group;
        this.ips = ips;
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

    public void setGroup(String group) {
        this.group = group;
    }

    public List<String> getIps() {
        return ips;
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
