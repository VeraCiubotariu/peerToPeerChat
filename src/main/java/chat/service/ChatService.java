package chat.service;

import chat.logic.MessageWrapper;

public interface ChatService {
    void closeAllConnections(MessageWrapper message);
    void closeConnection(MessageWrapper message);
    void startConnection(MessageWrapper message);
    void acknowledgeConnection(MessageWrapper message);
    void closeGroupConnection(MessageWrapper messageWrapper);
    void acknowledgeGroupConnection(MessageWrapper messageWrapper);
    void groupInvite(MessageWrapper messageWrapper);
}
