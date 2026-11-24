package com.fisiofino.model;

public class MalaltiaPacient {

    private String treatment;
    private String dniPacient;
    private String status;
    private String code;
    private String name;

    public MalaltiaPacient() {}

    public MalaltiaPacient(String treatment, String dniPacient, String status,
                           String code, String name) {
        this.treatment = treatment;
        this.dniPacient = dniPacient;
        this.status = status;
        this.code = code;
        this.name = name;
    }

    public String getTreatment() {
        return treatment;
    }
    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getDniPacient() {
        return dniPacient;
    }
    public void setDniPacient(String dniPacient) {
        this.dniPacient = dniPacient;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return code + " - " + name + " [" + status + "] " + treatment;
    }
}