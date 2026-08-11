package com.medilink;

import com.medilink.entity.AppUser;
import com.medilink.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DatabaseSeederTest {

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private DatabaseSeeder databaseSeeder;

    @BeforeEach
    public void setUp() {
        // Clear all users to ensure a clean slate
        userRepository.deleteAll();
    }

    @Test
    public void testAdminSeededWhenNoAdminExists() throws Exception {
        // Given - database is empty (cleared in setUp)
        assertTrue(userRepository.findAll().isEmpty());

        // When - we run the seeder manually (it also runs automatically on context startup)
        databaseSeeder.run();

        // Then - admin user should be seeded
        AppUser admin = userRepository.findByUsername("admin");
        assertNotNull(admin);
        assertEquals("admin123", admin.getPassword());
        assertEquals("ADMIN", admin.getRole());
    }

    @Test
    public void testAdminNotSeededWhenAdminAlreadyExists() throws Exception {
        // Given - admin user already exists
        AppUser existingAdmin = new AppUser();
        existingAdmin.setUsername("admin");
        existingAdmin.setPassword("customPassword");
        existingAdmin.setRole("ADMIN");
        userRepository.save(existingAdmin);

        long countBefore = userRepository.count();
        assertEquals(1, countBefore);

        // When - we run the seeder again
        databaseSeeder.run();

        // Then - count should remain 1 and password should not be overwritten
        assertEquals(1, userRepository.count());
        AppUser admin = userRepository.findByUsername("admin");
        assertNotNull(admin);
        assertEquals("customPassword", admin.getPassword());
    }
}
