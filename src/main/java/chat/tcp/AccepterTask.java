package chat.tcp;

import chat.loggers.Loggers;

public class AccepterTask implements Runnable {
    private Group group;

    public AccepterTask(Group group) {
        this.group = group;
    }

    @Override
    public void run() {
        while (true) {
            group.addParticipant();
            group.stopListeners();
            group.startListeners();
            Loggers.infoLogger.info("Added participant in accepter thread {}", Thread.currentThread().getName());
        }
    }
}
