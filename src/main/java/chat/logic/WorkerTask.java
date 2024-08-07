package chat.logic;

import chat.server.Server;

import java.util.Optional;

public class WorkerTask implements Runnable {
    private final Server server;
    private final MessageWrapper message;

    public WorkerTask(Server server, MessageWrapper message) {
        this.server = server;
        this.message = message;
    }

    private static Optional<Operation> parseOperations(final String message) {
        for (Operation op : Operation.values()) {
            if (MessageParser.parseMessage(message, op)) {
                return Optional.of(op);
            }
        }
        return Optional.empty();
    }

    @Override
    public void run() {
        Optional<Operation> operation = parseOperations(message.message().getMessage());

        synchronized (server) {
            operation.ifPresent(value -> value.operation.apply(server, message));
        }
    }
}
