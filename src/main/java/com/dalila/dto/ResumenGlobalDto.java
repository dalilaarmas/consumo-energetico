package com.dalila.dto;

import java.util.List;

/**
 * Este DTO es el "contrato" entre el servidor y tu panel visual.
 * Contiene toda la información del "Resumen General de Consumo".
 */
public class ResumenGlobalDto {

    // --- SECCIÓN: DÍAS RÉCORD ---
    private RegistroDTO diaMayorConsumo;
    private RegistroDTO diaMenorConsumo;

    // --- SECCIÓN: TOP 3 (Listas) ---
    private List<RegistroDTO> top3DiasMayor;
    private List<RegistroDTO> top3DiasMenor;

    // --- SECCIÓN: HITOS (Año/Mes) ---
    private String anioMasConsumo;
    private Double valorAnioMas;

    private String anioMenosConsumo;
    private Double valorAnioMenos;

    private String mesMenorConsumo;
    private Double valorMesMenor;

    // Constructor vacío
    public ResumenGlobalDto() {}

    // Constructor con todos los campos para facilitar la creación en el Service
    public ResumenGlobalDto(RegistroDTO diaMayorConsumo, RegistroDTO diaMenorConsumo,
                             List<RegistroDTO> top3DiasMayor, List<RegistroDTO> top3DiasMenor,
                             String anioMasConsumo, Double valorAnioMas,
                             String anioMenosConsumo, Double valorAnioMenos,
                             String mesMenorConsumo, Double valorMesMenor) {
        this.diaMayorConsumo = diaMayorConsumo;
        this.diaMenorConsumo = diaMenorConsumo;
        this.top3DiasMayor = top3DiasMayor;
        this.top3DiasMenor = top3DiasMenor;
        this.anioMasConsumo = anioMasConsumo;
        this.valorAnioMas = valorAnioMas;
        this.anioMenosConsumo = anioMenosConsumo;
        this.valorAnioMenos = valorAnioMenos;
        this.mesMenorConsumo = mesMenorConsumo;
        this.valorMesMenor = valorMesMenor;
    }

    public ResumenGlobalDto(Double total) {
    }

    // --- GETTERS Y SETTERS ---
    // (Son fundamentales para que la librería JSON pueda leer los datos)

    public RegistroDTO getDiaMayorConsumo() { return diaMayorConsumo; }
    public void setDiaMayorConsumo(RegistroDTO diaMayorConsumo) { this.diaMayorConsumo = diaMayorConsumo; }

    public RegistroDTO getDiaMenorConsumo() { return diaMenorConsumo; }
    public void setDiaMenorConsumo(RegistroDTO diaMenorConsumo) { this.diaMenorConsumo = diaMenorConsumo; }

    public List<RegistroDTO> getTop3DiasMayor() { return top3DiasMayor; }
    public void setTop3DiasMayor(List<RegistroDTO> top3DiasMayor) { this.top3DiasMayor = top3DiasMayor; }

    public List<RegistroDTO> getTop3DiasMenor() { return top3DiasMenor; }
    public void setTop3DiasMenor(List<RegistroDTO> top3DiasMenor) { this.top3DiasMenor = top3DiasMenor; }

    public String getAnioMasConsumo() { return anioMasConsumo; }
    public void setAnioMasConsumo(String anioMasConsumo) { this.anioMasConsumo = anioMasConsumo; }

    public Double getValorAnioMas() { return valorAnioMas; }
    public void setValorAnioMas(Double valorAnioMas) { this.valorAnioMas = valorAnioMas; }

    public String getAnioMenosConsumo() { return anioMenosConsumo; }
    public void setAnioMenosConsumo(String anioMenosConsumo) { this.anioMenosConsumo = anioMenosConsumo; }

    public Double getValorAnioMenos() { return valorAnioMenos; }
    public void setValorAnioMenos(Double valorAnioMenos) { this.valorAnioMenos = valorAnioMenos; }

    public String getMesMenorConsumo() { return mesMenorConsumo; }
    public void setMesMenorConsumo(String mesMenorConsumo) { this.mesMenorConsumo = mesMenorConsumo; }

    public Double getValorMesMenor() { return valorMesMenor; }
    public void setValorMesMenor(Double valorMesMenor) { this.valorMesMenor = valorMesMenor; }
}