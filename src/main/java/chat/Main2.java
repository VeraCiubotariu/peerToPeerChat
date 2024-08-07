package chat;

import chat.client.Client;

public class Main2 {
    public static void main(String[] args) throws InterruptedException {
        //Thread server = new Thread(new Server("E"));
        Thread client = new Thread(new Client("E"));

        client.start();
        //server.start();

        client.join();
        //server.join();
    }
}
