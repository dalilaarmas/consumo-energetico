package com.dalila.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO Maestro para el análisis detallado de un año específico.
 * Estructurado en tres niveles: Globales, Récords anuales y Desglose mensual.
 */
public class DetalleEstadisticoAnualDTO {

    // --- Nivel 1: Globales del año ---
    private int anio;
    private Double consumoTotalAnual;
    private Double promedioMensualAnual;

    // --- Nivel 2: Top 3 Absoluto del Año ---
    private List<RegistroDTO> top3AnualMayor;
    private List<RegistroDTO> top3AnualMenor;

    // --- Nivel 3: Desglose por Mes ---
    private Map<String, EstadisticasMensuales> detallePorMes;

    // --- CONSTRUCTORES ---
    public DetalleEstadisticoAnualDTO() {}

    // --- CLASE INTERNA PARA ESTADÍSTICAS MENSUALES ---
    public static class EstadisticasMensuales {
        private Double totalMes;
        private List<RegistroDTO> top3MayorMes;
        private List<RegistroDTO> top3MenorMes;

        public EstadisticasMensuales() {}

        public EstadisticasMensuales(Double total, List<RegistroDTO> mayor, List<RegistroDTO> menor) {
            this.totalMes = total;
            this.top3MayorMes = mayor;
            this.top3MenorMes = menor;
        }

        // Getters y Setters Clase Interna
        public Double getTotalMes() { return totalMes; }
        public void setTotalMes(Double totalMes) { this.totalMes = totalMes; }

        public List<RegistroDTO> getTop3MayorMes() { return top3MayorMes; }
        public void setTop3MayorMes(List<RegistroDTO> top3MayorMes) { this.top3MayorMes = top3MayorMes; }

        public List<RegistroDTO> getTop3MenorMes() { return top3MenorMes; }
        public void setTop3MenorMes(List<RegistroDTO> top3MenorMes) { this.top3MenorMes = top3MenorMes; }
    }

    // --- GETTERS Y SETTERS CLASE PRINCIPAL ---
    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }

    public Double getConsumoTotalAnual() { return consumoTotalAnual; }
    public void setConsumoTotalAnual(Double consumoTotalAnual) { this.consumoTotalAnual = consumoTotalAnual; }

    public Double getPromedioMensualAnual() { return promedioMensualAnual; }
    public void setPromedioMensualAnual(Double promedioMensualAnual) { this.promedioMensualAnual = promedioMensualAnual; }

    public List<RegistroDTO> getTop3AnualMayor() { return top3AnualMayor; }
    public void setTop3AnualMayor(List<RegistroDTO> top3AnualMayor) { this.top3AnualMayor = top3AnualMayor; }

    public List<RegistroDTO> getTop3AnualMenor() { return top3AnualMenor; }
    public void setTop3AnualMenor(List<RegistroDTO> top3AnualMenor) { this.top3AnualMenor = top3AnualMenor; }

    public Map<String, EstadisticasMensuales> getDetallePorMes() { return detallePorMes; }
    public void setDetallePorMes(Map<String, EstadisticasMensuales> detallePorMes) { this.detallePorMes = detallePorMes; }
}