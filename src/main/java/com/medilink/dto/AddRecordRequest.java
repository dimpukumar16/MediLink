package com.medilink.dto;

public class AddRecordRequest {
    private String patientUhid;
    private String diagnosis;
    private String prescription;
    private Long doctorId; // User ID of doctor
    
    public String getPatientUhid() { return patientUhid; }
    public void setPatientUhid(String patientUhid) { this.patientUhid = patientUhid; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getPrescription() { return prescription; }
    public void setPrescription(String prescription) { this.prescription = prescription; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
}
