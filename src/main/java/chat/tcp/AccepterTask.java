package chat.tcp;

import chat.loggers.Loggers;

public class AccepterTask implements Runnable {
    private Group group;

    public AccepterTask(Group group) {
        this.group = group;
    }

    @Override
    public void run() {
        Loggers.infoLogger.info("S-a pornit task-ul de asteptat");
        while (!Thread.currentThread().isInterrupted()) {
            group.addParticipant();
            group.stopListeners();
            group.startListeners();
        }
    }
}
