package de.immobiliencrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ImmobilienCrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImmobilienCrmApplication.class, args);
    }
}
