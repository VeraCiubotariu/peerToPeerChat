package chat;

import chat.client.Client;
import chat.server.Server;
import chat.utils.Usefullstuff;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Usefullstuff.getINSTANCE().setNickname("E");

        Thread server = new Thread(new Server());
        Thread client = new Thread(new Client());

        client.start();
        server.start();

        client.join();
        server.join();
    }
}
