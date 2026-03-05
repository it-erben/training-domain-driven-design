package de.realestate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class RealEstateCrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(RealEstateCrmApplication.class, args);
    }
}
