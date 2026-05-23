package com.medilink;

import com.medilink.entity.AppUser;
import com.medilink.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private AppUserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername("admin") == null) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println("System Admin seeded successfully: admin / admin123");
        }
    }
}
