package com.medilink.repository;

import com.medilink.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    Hospital findByUserId(Long userId);
}
