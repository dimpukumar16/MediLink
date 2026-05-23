package com.medilink.controller;

import com.medilink.dto.AddRecordRequest;
import com.medilink.entity.Doctor;
import com.medilink.entity.MedicalRecord;
import com.medilink.entity.Patient;
import com.medilink.repository.DoctorRepository;
import com.medilink.repository.MedicalRecordRepository;
import com.medilink.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    @Autowired private MedicalRecordRepository recordRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private DoctorRepository doctorRepository;

    @GetMapping("/{uhid}")
    public List<MedicalRecord> getRecords(@PathVariable String uhid) {
        return recordRepository.findByPatientUhidOrderByRecordDateDesc(uhid);
    }
    
    @GetMapping("/patient/search/{uhid}")
    public Patient searchPatient(@PathVariable String uhid) {
        Patient p = patientRepository.findByUhid(uhid);
        if (p == null) throw new RuntimeException("Patient not found");
        return p;
    }

    @PostMapping("/")
    public MedicalRecord addRecord(@RequestBody AddRecordRequest req) {
        Patient p = patientRepository.findByUhid(req.getPatientUhid());
        if (p == null) throw new RuntimeException("Patient not found");

        Doctor d = doctorRepository.findByUserId(req.getDoctorId());
        
        MedicalRecord record = new MedicalRecord();
        record.setPatientUhid(req.getPatientUhid());
        record.setDiagnosis(req.getDiagnosis());
        record.setPrescription(req.getPrescription());
        record.setRecordDate(LocalDateTime.now());
        if (d != null) {
            record.setDoctorName(d.getName());
            if (d.getHospital() != null) {
                record.setHospitalName(d.getHospital().getName());
            } else {
                record.setHospitalName("MediLink Network");
            }
        } else {
            record.setDoctorName("Unknown");
            record.setHospitalName("Unknown");
        }
        
        return recordRepository.save(record);
    }
}
