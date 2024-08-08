package chat.tcp;

public class AccepterTask implements Runnable {
    private Group group;

    public AccepterTask(Group group) {
        this.group = group;
    }

    @Override
    public void run() {
        while (true) {
            group.addParticipant();
        }
    }
}
