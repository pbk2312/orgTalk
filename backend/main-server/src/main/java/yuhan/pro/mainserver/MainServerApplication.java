package yuhan.pro.mainserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MainServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(MainServerApplication.class, args);
  }
}
