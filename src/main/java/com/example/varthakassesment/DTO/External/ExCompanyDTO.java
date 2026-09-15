package com.example.varthakassesment.DTO.External;

public class ExCompanyDTO {

    private String name;
    private String industry;
    private String role;
    private String website;
    private int employees;

    public ExCompanyDTO() {
    }

    public ExCompanyDTO(String name, String industry, String role, String website, int employees) {
        this.name = name;
        this.industry = industry;
        this.role = role;
        this.website = website;
        this.employees = employees;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public int getEmployees() {
        return employees;
    }

    public void setEmployees(int employees) {
        this.employees = employees;
    }
}
