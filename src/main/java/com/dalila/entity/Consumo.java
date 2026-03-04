package com.dalila.entity;
import java.math.BigDecimal;
import java.time.LocalDate;
public class Consumo {
    private long id;
    private String cupsCodigo;
    private LocalDate fecha;
    private BigDecimal consumo;

    public Consumo() {}

    public Consumo(long id, String cupsCodigo, LocalDate fecha, BigDecimal consumo) {
        this.id = id;
        this.cupsCodigo = cupsCodigo;
        this.fecha = fecha;
        this.consumo = consumo;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getCupsCodigo() { return cupsCodigo; }
    public void setCupsCodigo(String cupsCodigo) { this.cupsCodigo = cupsCodigo; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public BigDecimal getConsumo() { return consumo; }
    public void setConsumo(BigDecimal consumo) { this.consumo = consumo; }
}
