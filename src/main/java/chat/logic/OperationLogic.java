package chat.logic;

import chat.server.Server;

@FunctionalInterface
public interface OperationLogic {
    void apply(final Server server, final MessageWrapper message);
}
