package chat;

import chat.client.Client;
import chat.server.Server;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Thread server = new Thread(new Server("X"));
        Thread client = new Thread(new Client("X"));

        client.start();
        server.start();

        client.join();
        server.join();
    }
}
