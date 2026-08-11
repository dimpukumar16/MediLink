package com.medilink.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilink.dto.AddRecordRequest;
import com.medilink.entity.*;
import com.medilink.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MedicalRecordRepository recordRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private AppUserRepository userRepository;

    @BeforeEach
    public void cleanDatabase() {
        recordRepository.deleteAll();
        doctorRepository.deleteAll();
        hospitalRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testGetRecordsOrderByDateDesc() throws Exception {
        // Seed Patient
        AppUser pUser = new AppUser();
        pUser.setUsername("pat"); pUser.setPassword("p"); pUser.setRole("PATIENT");
        pUser = userRepository.save(pUser);

        Patient p = new Patient();
        p.setUser(pUser); p.setUhid("ML-UHID123"); p.setName("Alice");
        patientRepository.save(p);

        // Seed some medical records with varying timestamps
        MedicalRecord rec1 = new MedicalRecord();
        rec1.setPatientUhid("ML-UHID123");
        rec1.setDiagnosis("Flu");
        rec1.setPrescription("Rest");
        rec1.setDoctorName("Dr. A");
        rec1.setHospitalName("Hosp A");
        rec1.setRecordDate(LocalDateTime.now().minusDays(2));
        recordRepository.save(rec1);

        MedicalRecord rec2 = new MedicalRecord();
        rec2.setPatientUhid("ML-UHID123");
        rec2.setDiagnosis("Covid");
        rec2.setPrescription("Vaccine");
        rec2.setDoctorName("Dr. B");
        rec2.setHospitalName("Hosp B");
        rec2.setRecordDate(LocalDateTime.now().minusDays(1));
        recordRepository.save(rec2);

        // Fetch records - should return ordered by recordDate desc (Covid first, then Flu)
        mockMvc.perform(get("/api/records/ML-UHID123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].diagnosis", is("Covid")))
                .andExpect(jsonPath("$[1].diagnosis", is("Flu")));
    }

    @Test
    public void testSearchPatientSuccess() throws Exception {
        // Seed Patient
        AppUser pUser = new AppUser();
        pUser.setUsername("pat2"); pUser.setPassword("p"); pUser.setRole("PATIENT");
        pUser = userRepository.save(pUser);

        Patient p = new Patient();
        p.setUser(pUser); p.setUhid("ML-SEARCH1"); p.setName("Bob");
        patientRepository.save(p);

        mockMvc.perform(get("/api/records/patient/search/ML-SEARCH1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Bob")))
                .andExpect(jsonPath("$.uhid", is("ML-SEARCH1")));
    }

    @Test
    public void testSearchPatientNotFound() throws Exception {
        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(get("/api/records/patient/search/ML-UNKNOWN"));
        });
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Patient not found", exception.getCause().getMessage());
    }

    @Test
    public void testAddRecordSuccessWithDoctorAndHospital() throws Exception {
        // Seed Patient
        AppUser pUser = new AppUser();
        pUser.setUsername("pat3"); pUser.setPassword("p"); pUser.setRole("PATIENT");
        pUser = userRepository.save(pUser);

        Patient p = new Patient();
        p.setUser(pUser); p.setUhid("ML-PAT3"); p.setName("Charlie");
        patientRepository.save(p);

        // Seed Hospital
        AppUser hUser = new AppUser();
        hUser.setUsername("hosp"); hUser.setPassword("p"); hUser.setRole("HOSPITAL");
        hUser = userRepository.save(hUser);
        Hospital hosp = new Hospital();
        hosp.setUser(hUser); hosp.setName("City General");
        hosp = hospitalRepository.save(hosp);

        // Seed Doctor
        AppUser dUser = new AppUser();
        dUser.setUsername("doc"); dUser.setPassword("p"); dUser.setRole("DOCTOR");
        dUser = userRepository.save(dUser);
        Doctor doc = new Doctor();
        doc.setUser(dUser); doc.setName("Dr. Gregory"); doc.setHospital(hosp);
        doctorRepository.save(doc);

        AddRecordRequest req = new AddRecordRequest();
        req.setPatientUhid("ML-PAT3");
        req.setDoctorId(dUser.getId()); // Using AppUser ID as DoctorRepository lookup matches by userId
        req.setDiagnosis("Migraine");
        req.setPrescription("Aspirin");

        mockMvc.perform(post("/api/records/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientUhid", is("ML-PAT3")))
                .andExpect(jsonPath("$.diagnosis", is("Migraine")))
                .andExpect(jsonPath("$.prescription", is("Aspirin")))
                .andExpect(jsonPath("$.doctorName", is("Dr. Gregory")))
                .andExpect(jsonPath("$.hospitalName", is("City General")))
                .andExpect(jsonPath("$.recordDate", notNullValue()));

        List<MedicalRecord> records = recordRepository.findByPatientUhidOrderByRecordDateDesc("ML-PAT3");
        assertEquals(1, records.size());
        assertEquals("Migraine", records.get(0).getDiagnosis());
    }

    @Test
    public void testAddRecordSuccessDoctorNoHospital() throws Exception {
        // Seed Patient
        AppUser pUser = new AppUser();
        pUser.setUsername("pat4"); pUser.setPassword("p"); pUser.setRole("PATIENT");
        pUser = userRepository.save(pUser);

        Patient p = new Patient();
        p.setUser(pUser); p.setUhid("ML-PAT4"); p.setName("Dave");
        patientRepository.save(p);

        // Seed Doctor with no hospital
        AppUser dUser = new AppUser();
        dUser.setUsername("doc2"); dUser.setPassword("p"); dUser.setRole("DOCTOR");
        dUser = userRepository.save(dUser);
        Doctor doc = new Doctor();
        doc.setUser(dUser); doc.setName("Dr. Private");
        doctorRepository.save(doc);

        AddRecordRequest req = new AddRecordRequest();
        req.setPatientUhid("ML-PAT4");
        req.setDoctorId(dUser.getId());
        req.setDiagnosis("Cold");
        req.setPrescription("Teas");

        mockMvc.perform(post("/api/records/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorName", is("Dr. Private")))
                .andExpect(jsonPath("$.hospitalName", is("MediLink Network")));
    }

    @Test
    public void testAddRecordSuccessUnknownDoctor() throws Exception {
        // Seed Patient
        AppUser pUser = new AppUser();
        pUser.setUsername("pat5"); pUser.setPassword("p"); pUser.setRole("PATIENT");
        pUser = userRepository.save(pUser);

        Patient p = new Patient();
        p.setUser(pUser); p.setUhid("ML-PAT5"); p.setName("Eva");
        patientRepository.save(p);

        AddRecordRequest req = new AddRecordRequest();
        req.setPatientUhid("ML-PAT5");
        req.setDoctorId(9999L); // Unknown doctor id
        req.setDiagnosis("Allergy");
        req.setPrescription("Antihistamine");

        mockMvc.perform(post("/api/records/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorName", is("Unknown")))
                .andExpect(jsonPath("$.hospitalName", is("Unknown")));
    }

    @Test
    public void testAddRecordPatientNotFound() throws Exception {
        AddRecordRequest req = new AddRecordRequest();
        req.setPatientUhid("ML-NOTEXIST");
        req.setDoctorId(1L);
        req.setDiagnosis("Allergy");
        req.setPrescription("Antihistamine");

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/api/records/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)));
        });
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Patient not found", exception.getCause().getMessage());
    }
}
