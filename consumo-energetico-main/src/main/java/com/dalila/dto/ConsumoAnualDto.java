package com.dalila.dto;

public class ConsumoAnualDto {
    private int anio;
    private Double consumoTotal;
    private Double promedioMensual;
    private String mesMasAlto;
    private Double valorMesMasAlto;

    // 1. CONSTRUCTOR VACÍO
    // Obligatorio para que las librerías de Java puedan convertir esto a JSON
    public ConsumoAnualDto() {}

    // 2. CONSTRUCTOR COMPLETO
    // Útil para crear el objeto rápidamente desde tu lógica de cálculo en el Resource o Service
    public ConsumoAnualDto(int anio, Double consumoTotal, Double promedioMensual, String mesMasAlto, Double valorMesMasAlto) {
        this.anio = anio;
        this.consumoTotal = consumoTotal;
        this.promedioMensual = promedioMensual;
        this.mesMasAlto = mesMasAlto;
        this.valorMesMasAlto = valorMesMasAlto;
    }
    public ConsumoAnualDto(int anio, Double consumoTotal) {
        this.anio = anio;
        this.consumoTotal = consumoTotal;
    }

    // 3. GETTERS Y SETTERS
    // Sin estos métodos, el servidor no podrá "leer" los datos para enviarlos al navegador

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }

    public Double getConsumoTotal() { return consumoTotal; }
    public void setConsumoTotal(Double consumoTotal) { this.consumoTotal = consumoTotal; }

    public Double getPromedioMensual() { return promedioMensual; }
    public void setPromedioMensual(Double promedioMensual) { this.promedioMensual = promedioMensual; }

    public String getMesMasAlto() { return mesMasAlto; }
    public void setMesMasAlto(String mesMasAlto) { this.mesMasAlto = mesMasAlto; }

    public Double getValorMesMasAlto() { return valorMesMasAlto; }
    public void setValorMesMasAlto(Double valorMesMasAlto) { this.valorMesMasAlto = valorMesMasAlto; }
}