package com.medilink.dto;

public class LoginResponse {
    private String token;
    private String role;
    private String uhid;
    private Long id;
    private String name;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getUhid() { return uhid; }
    public void setUhid(String uhid) { this.uhid = uhid; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
