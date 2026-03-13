package com.dalila.entity;

public class Cups {
    private String codigo;
    private String direccion;
    private Integer codigoPostal;
    private int municipioId;
    private int distribuidorId;

    public Cups() {
    }

    public Cups(String codigo, String direccion, Integer codigoPostal, int municipioId, int distribuidorId) {
        this.codigo = codigo;
        this.direccion = direccion;
        this.codigoPostal = codigoPostal;
        this.municipioId = municipioId;
        this.distribuidorId = distribuidorId;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Integer getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(Integer codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public int getMunicipioId() {
        return municipioId;
    }

    public void setMunicipioId(int municipioId) {
        this.municipioId = municipioId;
    }

    public int getDistribuidorId() {
        return distribuidorId;
    }

    public void setDistribuidorId(int distribuidorId) {
        this.distribuidorId = distribuidorId;
    }
}
