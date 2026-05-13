package com.dalila.dto;

public class RegistroDTO {
    private int id;
    private String municipio;
    private String cups;
    private String direccion;
    private String fecha;
    private Double consumo;

    public RegistroDTO() {
    }

    public RegistroDTO(int id, String municipio, String cups, String direccion, String fecha, Double consumo) {
        this.id = id;
        this.municipio = municipio;
        this.cups = cups;
        this.direccion = direccion;
        this.fecha = fecha;
        this.consumo = consumo;
    }

    public int getId() {
        return id;
    }

    public String getMunicipio() {
        return municipio;
    }

    public String getCups() {
        return cups;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getFecha() {
        return fecha;
    }

    public Double getConsumo() {
        return consumo;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public void setCups(String cups) {
        this.cups = cups;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setConsumo(Double consumo) {
        this.consumo = consumo;
    }
}