package com.dalila.dto;

public class CupsDto {

    private String codigo;
    private String direccion;
    private Integer codigoPostal;

    public CupsDto() {
    }

    public CupsDto(String codigo, String direccion, Integer codigoPostal) {
        this.codigo = codigo;
        this.direccion = direccion;
        this.codigoPostal = codigoPostal;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDireccion() {
        return direccion;
    }
    public Integer getCodigoPostal() {
        return codigoPostal;
    }
}