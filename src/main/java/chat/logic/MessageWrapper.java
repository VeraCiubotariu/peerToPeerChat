package chat.logic;

import java.net.InetAddress;

public record MessageWrapper(Message message, InetAddress senderIp) {
}
