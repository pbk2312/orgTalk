package yuhan.pro.chatserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.mongodb.config.EnableMongoAuditing;


@EnableJpaAuditing
@EnableMongoAuditing
@SpringBootApplication
public class ChatServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ChatServerApplication.class, args);
  }
}
