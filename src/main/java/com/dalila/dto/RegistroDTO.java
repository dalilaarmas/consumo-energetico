package com.dalila.dto;

public class RegistroDTO {

    public int id;
    public String municipio;
    public String cups;
    public String direccion;
    public String fecha;
    public Double consumo;

    public RegistroDTO() {}

    public RegistroDTO(int id, String municipio, String cups, String direccion, String fecha, Double consumo) {
        this.id = id;
        this.municipio = municipio;
        this.cups = cups;
        this.direccion = direccion;
        this.fecha = fecha;
        this.consumo = consumo;
    }
}