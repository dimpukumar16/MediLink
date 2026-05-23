package com.medilink.dto;

public class RegisterDoctorRequest {
    private String username;
    private String password;
    private String name;
    private String specialization;
    private Long hospitalUserId;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public Long getHospitalUserId() { return hospitalUserId; }
    public void setHospitalUserId(Long hospitalUserId) { this.hospitalUserId = hospitalUserId; }
}
