package com.dalila.service;

import com.dalila.dao.RegistroDao;
import com.dalila.dto.*;

import java.util.List;

/**
 * Servicio de Registros. Centraliza toda la lógica de negocio de consumo:
 * CRUD, filtrado, estadísticas y PDF.
 *
 * Los Resources NUNCA acceden a RegistroDao ni EstadisticasService directamente.
 */
public class RegistroService {

    private final RegistroDao         registroDao         = new RegistroDao();
    private final EstadisticasService estadisticasService = new EstadisticasService();

    // ── Consultas ─────────────────────────────────────────────────────────────

    public List<RegistroDTO> findAll() {
        return registroDao.findAll();
    }

    /**
     * Busca un registro por ID. Devuelve null si no existe.
     */
    public RegistroDTO findById(int id) {
        return registroDao.findById(id);
    }

    /**
     * Filtra registros por cualquier combinación de criterios.
     * Todos los parámetros son opcionales (null = sin filtro).
     * fechaDesde/Hasta aceptan: "2023", "2023-05", "2023-05-15"
     */
    public List<RegistroDTO> findFiltered(String municipio, String cups, String direccion,
                                          String fechaDesde, String fechaHasta,
                                          Double consumoMin, Double consumoMax) {
        return registroDao.findFiltered(
                municipio, cups, direccion, fechaDesde, fechaHasta, consumoMin, consumoMax
        );
    }

    // ── Estadísticas ──────────────────────────────────────────────────────────

    /**
     * Resumen global: día máx/mín, top 3, año con más/menos consumo, mes mínimo.
     */
    public ResumenGlobalDto getResumenGlobal() {
        return estadisticasService.calcularResumenGlobal(registroDao.findAll());
    }

    /**
     * Total de consumo agrupado por año, de más reciente a más antiguo.
     */
    public List<ConsumoAnualDto> getResumenAnual() {
        return estadisticasService.calcularResumenAnual(registroDao.findAll());
    }

    /**
     * Registros individuales de un año concreto.
     */
    public List<RegistroDTO> getRegistrosPorAnio(int anio) {
        return registroDao.findAll().stream()
                .filter(r -> r.getFecha() != null && r.getFecha().startsWith(String.valueOf(anio)))
                .toList();
    }

    /**
     * Análisis estadístico completo de un año: total, promedio mensual,
     * mes más alto, top 3 días mayor y menor consumo, desglose por mes.
     * Devuelve null si no hay datos para ese año.
     */
    public DetalleEstadisticoAnualDTO getAnalisisPorAnio(int anio) {
        return estadisticasService.obtenerAnalisisCompleto(anio, registroDao.findAll());
    }

    // ── Escritura ─────────────────────────────────────────────────────────────

    /**
     * Crea un nuevo registro de consumo.
     * Lanza IllegalArgumentException si faltan campos obligatorios.
     */
    public RegistroDTO crear(RegistroDTO dto) {
        validarRegistro(dto);
        int id = registroDao.insert(dto);
        dto.setId(id);
        return dto;
    }

    /**
     * Actualiza cups_codigo, fecha y consumo de un registro existente.
     * Lanza IllegalArgumentException si no existe o faltan campos.
     */
    public RegistroDTO actualizar(int id, RegistroDTO dto) {
        if (registroDao.findById(id) == null)
            throw new IllegalArgumentException("Registro no encontrado: " + id);
        validarRegistro(dto);
        dto.setId(id);
        registroDao.update(id, dto);
        return registroDao.findById(id);
    }

    /**
     * Elimina un registro por ID.
     * Lanza IllegalArgumentException si no existe.
     */
    public void eliminar(int id) {
        if (registroDao.findById(id) == null)
            throw new IllegalArgumentException("Registro no encontrado: " + id);
        registroDao.delete(id);
    }

    // ── Validaciones ──────────────────────────────────────────────────────────

    private void validarRegistro(RegistroDTO dto) {
        if (dto.getCups() == null || dto.getCups().isBlank())
            throw new IllegalArgumentException("El campo 'cups' es obligatorio");
        if (dto.getFecha() == null || dto.getFecha().isBlank())
            throw new IllegalArgumentException("El campo 'fecha' es obligatorio (formato: YYYY-MM-DD)");
        if (dto.getConsumo() == null)
            throw new IllegalArgumentException("El campo 'consumo' es obligatorio");
    }
}