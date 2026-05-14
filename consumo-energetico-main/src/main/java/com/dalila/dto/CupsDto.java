package com.dalila.dto;

/**
 * DTO de CUPS. Incluye el nombre del municipio para no requerir
 * una segunda llamada al cliente.
 */
public class CupsDto {

    private String  codigo;
    private String  direccion;
    private Integer codigoPostal;
    private Integer municipioId;
    private String  municipio;        // nombre del municipio
    private Integer distribuidorId;

    public CupsDto() {}

    public CupsDto(String codigo, String direccion, Integer codigoPostal,
                   Integer municipioId, String municipio, Integer distribuidorId) {
        this.codigo         = codigo;
        this.direccion      = direccion;
        this.codigoPostal   = codigoPostal;
        this.municipioId    = municipioId;
        this.municipio      = municipio;
        this.distribuidorId = distribuidorId;
    }

    // Getters
    public String  getCodigo()         { return codigo; }
    public String  getDireccion()      { return direccion; }
    public Integer getCodigoPostal()   { return codigoPostal; }
    public Integer getMunicipioId()    { return municipioId; }
    public String  getMunicipio()      { return municipio; }
    public Integer getDistribuidorId() { return distribuidorId; }

    // Setters
    public void setCodigo(String codigo)                   { this.codigo = codigo; }
    public void setDireccion(String direccion)             { this.direccion = direccion; }
    public void setCodigoPostal(Integer codigoPostal)      { this.codigoPostal = codigoPostal; }
    public void setMunicipioId(Integer municipioId)        { this.municipioId = municipioId; }
    public void setMunicipio(String municipio)             { this.municipio = municipio; }
    public void setDistribuidorId(Integer distribuidorId)  { this.distribuidorId = distribuidorId; }
}
