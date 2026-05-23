package com.medilink.controller;

import com.medilink.dto.*;
import com.medilink.entity.*;
import com.medilink.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired private AppUserRepository userRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private HospitalRepository hospitalRepository;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        AppUser user = userRepository.findByUsername(req.getUsername());
        if (user != null && user.getPassword().equals(req.getPassword())) {
            LoginResponse res = new LoginResponse();
            res.setToken("dummy-token-" + user.getId());
            res.setRole(user.getRole());
            res.setId(user.getId());
            if ("PATIENT".equals(user.getRole())) {
                Patient p = patientRepository.findByUserId(user.getId());
                res.setUhid(p.getUhid());
                res.setName(p.getName());
            } else if ("DOCTOR".equals(user.getRole())) {
                Doctor d = doctorRepository.findByUserId(user.getId());
                res.setName(d.getName());
            } else if ("HOSPITAL".equals(user.getRole())) {
                Hospital h = hospitalRepository.findByUserId(user.getId());
                res.setName(h.getName());
            } else if ("ADMIN".equals(user.getRole())) {
                res.setName("System Administrator");
            }
            return res;
        }
        throw new RuntimeException("Invalid credentials");
    }

    @PostMapping("/register/patient")
    public Patient registerPatient(@RequestBody RegisterPatientRequest req) {
        if (userRepository.findByUsername(req.getUsername()) != null) {
            throw new RuntimeException("Username already exists");
        }
        AppUser user = new AppUser();
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword());
        user.setRole("PATIENT");
        user = userRepository.save(user);

        Patient p = new Patient();
        p.setUser(user);
        p.setUhid("ML-" + UUID.randomUUID().toString().substring(0,8).toUpperCase());
        p.setName(req.getName());
        p.setAge(req.getAge());
        p.setBloodGroup(req.getBloodGroup());
        return patientRepository.save(p);
    }

    @PostMapping("/register/doctor")
    public Doctor registerDoctor(@RequestBody RegisterDoctorRequest req) {
        if (userRepository.findByUsername(req.getUsername()) != null) {
            throw new RuntimeException("Username already exists");
        }
        AppUser user = new AppUser();
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword());
        user.setRole("DOCTOR");
        user = userRepository.save(user);

        Doctor d = new Doctor();
        d.setUser(user);
        d.setName(req.getName());
        d.setSpecialization(req.getSpecialization());
        
        if (req.getHospitalUserId() != null) {
            Hospital h = hospitalRepository.findByUserId(req.getHospitalUserId());
            d.setHospital(h);
        }
        
        return doctorRepository.save(d);
    }

    @PostMapping("/register/hospital")
    public Hospital registerHospital(@RequestBody RegisterHospitalRequest req) {
        if (userRepository.findByUsername(req.getUsername()) != null) {
            throw new RuntimeException("Username already exists");
        }
        AppUser user = new AppUser();
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword());
        user.setRole("HOSPITAL");
        user = userRepository.save(user);

        Hospital h = new Hospital();
        h.setUser(user);
        h.setName(req.getName());
        h.setAddress(req.getAddress());
        return hospitalRepository.save(h);
    }

    @GetMapping("/hospitals")
    public java.util.List<Hospital> getHospitals() {
        return hospitalRepository.findAll();
    }

    @GetMapping("/hospitals/{userId}/doctors")
    public java.util.List<Doctor> getHospitalDoctors(@PathVariable Long userId) {
        Hospital h = hospitalRepository.findByUserId(userId);
        if (h == null) return java.util.Collections.emptyList();
        return doctorRepository.findByHospitalId(h.getId());
    }
}
