package com.cotefacil.gateway.config;

import com.cotefacil.gateway.model.User;
import com.cotefacil.gateway.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("usuario").isEmpty()) {
                User user = new User();
                user.setUsername("usuario");
                user.setPassword(passwordEncoder.encode("senha123"));
                user.setRoles(List.of("USER"));
                userRepository.save(user);
                log.info("Usuário padrão criado: usuario");
            }
        };
    }
}
