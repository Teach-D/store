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
                Role userRole = Role
                        .builder()
                        .roleId(1L)
                        .name("ROLE_USER")
                        .build();


                Role adminRole = Role
                        .builder()
                        .roleId(2L)
                        .name("ROLE_ADMIN")
                        .build();


                roleRepository.save(userRole);
                roleRepository.save(adminRole);
            }
        };
    }
}
