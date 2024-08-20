package chat.rest;

import chat.server.Server;
import chat.utils.ChatUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@ComponentScan({"chat.websocket", "chat.services", "chat.rest"})
@SpringBootApplication
@EnableWebMvc
public class StartRestServices {
    public static void main(String[] args) {
        ChatUtils.getINSTANCE().setNickname("E");
        Thread server = new Thread(new Server());
        server.start();
        SpringApplication.run(StartRestServices.class, args);
    }
}
