package com.medilink.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilink.dto.*;
import com.medilink.entity.*;
import com.medilink.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @BeforeEach
    public void cleanDatabase() {
        // Delete records in reverse dependency order
        doctorRepository.deleteAll();
        hospitalRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testLoginAdminSuccess() throws Exception {
        // Seed admin user
        AppUser admin = new AppUser();
        admin.setUsername("admin");
        admin.setPassword("admin123");
        admin.setRole("ADMIN");
        userRepository.save(admin);

        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", startsWith("dummy-token-")))
                .andExpect(jsonPath("$.role", is("ADMIN")))
                .andExpect(jsonPath("$.name", is("System Administrator")));
    }

    @Test
    public void testLoginPatientSuccess() throws Exception {
        // Register patient
        AppUser user = new AppUser();
        user.setUsername("patientUser");
        user.setPassword("pass123");
        user.setRole("PATIENT");
        user = userRepository.save(user);

        Patient p = new Patient();
        p.setUser(user);
        p.setUhid("ML-12345678");
        p.setName("John Doe");
        p.setAge(30);
        p.setBloodGroup("O+");
        patientRepository.save(p);

        LoginRequest req = new LoginRequest();
        req.setUsername("patientUser");
        req.setPassword("pass123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("PATIENT")))
                .andExpect(jsonPath("$.uhid", is("ML-12345678")))
                .andExpect(jsonPath("$.name", is("John Doe")));
    }

    @Test
    public void testLoginDoctorSuccess() throws Exception {
        // Register doctor
        AppUser user = new AppUser();
        user.setUsername("doctorUser");
        user.setPassword("docpass");
        user.setRole("DOCTOR");
        user = userRepository.save(user);

        Doctor d = new Doctor();
        d.setUser(user);
        d.setName("Dr. Smith");
        d.setSpecialization("Cardiology");
        doctorRepository.save(d);

        LoginRequest req = new LoginRequest();
        req.setUsername("doctorUser");
        req.setPassword("docpass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("DOCTOR")))
                .andExpect(jsonPath("$.name", is("Dr. Smith")));
    }

    @Test
    public void testLoginHospitalSuccess() throws Exception {
        // Register hospital
        AppUser user = new AppUser();
        user.setUsername("hospitalUser");
        user.setPassword("hospass");
        user.setRole("HOSPITAL");
        user = userRepository.save(user);

        Hospital h = new Hospital();
        h.setUser(user);
        h.setName("General Hospital");
        h.setAddress("123 Med Ave");
        hospitalRepository.save(h);

        LoginRequest req = new LoginRequest();
        req.setUsername("hospitalUser");
        req.setPassword("hospass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("HOSPITAL")))
                .andExpect(jsonPath("$.name", is("General Hospital")));
    }

    @Test
    public void testLoginFailure() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("nonexistent");
        req.setPassword("wrong");

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)));
        });
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Invalid credentials", exception.getCause().getMessage());
    }

    @Test
    public void testRegisterPatientSuccess() throws Exception {
        RegisterPatientRequest req = new RegisterPatientRequest();
        req.setUsername("newpatient");
        req.setPassword("patientpass");
        req.setName("Alice");
        req.setAge(25);
        req.setBloodGroup("A-");

        mockMvc.perform(post("/api/auth/register/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uhid", startsWith("ML-")))
                .andExpect(jsonPath("$.name", is("Alice")))
                .andExpect(jsonPath("$.age", is(25)))
                .andExpect(jsonPath("$.bloodGroup", is("A-")));

        // Verify entity persisted in DB
        AppUser user = userRepository.findByUsername("newpatient");
        assertNotNull(user);
        assertEquals("PATIENT", user.getRole());
        Patient p = patientRepository.findByUserId(user.getId());
        assertNotNull(p);
        assertEquals("Alice", p.getName());
    }

    @Test
    public void testRegisterPatientDuplicateUsername() throws Exception {
        // Seed username
        AppUser user = new AppUser();
        user.setUsername("patient1");
        user.setPassword("pass");
        user.setRole("PATIENT");
        userRepository.save(user);

        RegisterPatientRequest req = new RegisterPatientRequest();
        req.setUsername("patient1");
        req.setPassword("newpass");
        req.setName("Alice");
        req.setAge(25);
        req.setBloodGroup("A-");

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/api/auth/register/patient")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)));
        });
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Username already exists", exception.getCause().getMessage());
    }

    @Test
    public void testRegisterDoctorSuccessWithoutHospital() throws Exception {
        RegisterDoctorRequest req = new RegisterDoctorRequest();
        req.setUsername("doctor1");
        req.setPassword("docpass");
        req.setName("Dr. House");
        req.setSpecialization("Diagnostics");

        mockMvc.perform(post("/api/auth/register/doctor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Dr. House")))
                .andExpect(jsonPath("$.specialization", is("Diagnostics")))
                .andExpect(jsonPath("$.hospital", nullValue()));
    }

    @Test
    public void testRegisterDoctorSuccessWithHospital() throws Exception {
        // Seed Hospital first
        AppUser hospUser = new AppUser();
        hospUser.setUsername("hosp1");
        hospUser.setPassword("pass");
        hospUser.setRole("HOSPITAL");
        hospUser = userRepository.save(hospUser);

        Hospital hosp = new Hospital();
        hosp.setUser(hospUser);
        hosp.setName("St. Jude");
        hosp.setAddress("Memphis");
        hosp = hospitalRepository.save(hosp);

        RegisterDoctorRequest req = new RegisterDoctorRequest();
        req.setUsername("doctor2");
        req.setPassword("docpass");
        req.setName("Dr. Wilson");
        req.setSpecialization("Oncology");
        req.setHospitalUserId(hospUser.getId()); // Pass User ID as requested in registerDoctor

        mockMvc.perform(post("/api/auth/register/doctor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Dr. Wilson")))
                .andExpect(jsonPath("$.specialization", is("Oncology")))
                .andExpect(jsonPath("$.hospital.name", is("St. Jude")));
    }

    @Test
    public void testRegisterDoctorDuplicateUsername() throws Exception {
        // Seed username
        AppUser user = new AppUser();
        user.setUsername("doctorDuplicate");
        user.setPassword("pass");
        user.setRole("DOCTOR");
        userRepository.save(user);

        RegisterDoctorRequest req = new RegisterDoctorRequest();
        req.setUsername("doctorDuplicate");
        req.setPassword("docpass");
        req.setName("Dr. House");
        req.setSpecialization("Diagnostics");

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/api/auth/register/doctor")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)));
        });
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Username already exists", exception.getCause().getMessage());
    }

    @Test
    public void testRegisterHospitalSuccess() throws Exception {
        RegisterHospitalRequest req = new RegisterHospitalRequest();
        req.setUsername("hosp2");
        req.setPassword("hosppass");
        req.setName("Mayo Clinic");
        req.setAddress("Rochester");

        mockMvc.perform(post("/api/auth/register/hospital")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Mayo Clinic")))
                .andExpect(jsonPath("$.address", is("Rochester")));

        AppUser user = userRepository.findByUsername("hosp2");
        assertNotNull(user);
        assertEquals("HOSPITAL", user.getRole());
        Hospital h = hospitalRepository.findByUserId(user.getId());
        assertNotNull(h);
        assertEquals("Mayo Clinic", h.getName());
    }

    @Test
    public void testRegisterHospitalDuplicateUsername() throws Exception {
        // Seed username
        AppUser user = new AppUser();
        user.setUsername("hospDuplicate");
        user.setPassword("pass");
        user.setRole("HOSPITAL");
        userRepository.save(user);

        RegisterHospitalRequest req = new RegisterHospitalRequest();
        req.setUsername("hospDuplicate");
        req.setPassword("hosppass");
        req.setName("Mayo Clinic");
        req.setAddress("Rochester");

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/api/auth/register/hospital")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)));
        });
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Username already exists", exception.getCause().getMessage());
    }

    @Test
    public void testGetHospitals() throws Exception {
        // Seed some hospitals
        AppUser u1 = new AppUser(); u1.setUsername("h1"); u1.setPassword("p"); u1.setRole("HOSPITAL"); userRepository.save(u1);
        Hospital h1 = new Hospital(); h1.setUser(u1); h1.setName("Hosp A"); h1.setAddress("Addr A"); hospitalRepository.save(h1);

        AppUser u2 = new AppUser(); u2.setUsername("h2"); u2.setPassword("p"); u2.setRole("HOSPITAL"); userRepository.save(u2);
        Hospital h2 = new Hospital(); h2.setUser(u2); h2.setName("Hosp B"); h2.setAddress("Addr B"); hospitalRepository.save(h2);

        mockMvc.perform(get("/api/auth/hospitals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Hosp A")))
                .andExpect(jsonPath("$[1].name", is("Hosp B")));
    }

    @Test
    public void testGetHospitalDoctors() throws Exception {
        // Seed Hospital
        AppUser hospUser = new AppUser(); hospUser.setUsername("hosp"); hospUser.setPassword("p"); hospUser.setRole("HOSPITAL"); userRepository.save(hospUser);
        Hospital hosp = new Hospital(); hosp.setUser(hospUser); hosp.setName("City Clinic"); hosp.setAddress("Main St"); hospitalRepository.save(hosp);

        // Seed Doctor linked to Hospital
        AppUser docUser = new AppUser(); docUser.setUsername("doc"); docUser.setPassword("p"); docUser.setRole("DOCTOR"); userRepository.save(docUser);
        Doctor doc = new Doctor(); doc.setUser(docUser); doc.setName("Dr. Watson"); doc.setSpecialization("GP"); doc.setHospital(hosp); doctorRepository.save(doc);

        // Fetch doctors by hospital's USER ID
        mockMvc.perform(get("/api/auth/hospitals/" + hospUser.getId() + "/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Dr. Watson")));
    }

    @Test
    public void testGetHospitalDoctorsNotFound() throws Exception {
        mockMvc.perform(get("/api/auth/hospitals/9999/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
