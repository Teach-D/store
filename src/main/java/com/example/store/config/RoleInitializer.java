package com.example.store.config;

import com.example.store.entity.Role;
import com.example.store.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoleInitializer {

    @Bean
    public CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            if(roleRepository.count() == 0) {
                Role userRole = new Role();
                userRole.setRoleId(1L);
                userRole.setName("ROLE_USER");

                Role adminRole = new Role();
                adminRole.setRoleId(2L);
                adminRole.setName("ROLE_ADMIN");

                roleRepository.save(userRole);
                roleRepository.save(adminRole);
            }
        };
    }
}
