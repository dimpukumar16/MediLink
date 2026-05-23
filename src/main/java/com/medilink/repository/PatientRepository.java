package com.medilink.repository;

import com.medilink.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Patient findByUhid(String uhid);
    Patient findByUserId(Long userId);
}
