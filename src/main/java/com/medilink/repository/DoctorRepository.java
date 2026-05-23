package com.medilink.repository;

import com.medilink.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Doctor findByUserId(Long userId);
    java.util.List<Doctor> findByHospitalId(Long hospitalId);
}
