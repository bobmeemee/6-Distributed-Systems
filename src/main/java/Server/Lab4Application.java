package Server;

import Node.Node;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Lab4Application {

    public static void main(String[] args) {
        SpringApplication springApp = new SpringApplication(Lab4Application.class);
        springApp.run(args);
        }
}


