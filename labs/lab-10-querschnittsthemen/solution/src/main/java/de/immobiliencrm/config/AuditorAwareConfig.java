package de.immobiliencrm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * Configuration providing the AuditorAware bean for JPA auditing.
 * Returns a fixed "system" user. In a real application, this would resolve
 * the currently authenticated user from the security context.
 */
@Configuration
public class AuditorAwareConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("system");
    }
}
