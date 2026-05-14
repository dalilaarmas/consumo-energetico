package com.dalila.service;

import com.dalila.dao.CupsDao;
import com.dalila.dao.MunicipioDao;
import com.dalila.dto.CupsDto;
import com.dalila.dto.MunicipioDto;
import com.dalila.entity.Municipio;

import java.util.List;

/**
 * Servicio de Municipios. Toda la lógica de negocio relacionada con municipios.
 * Los Resources solo llaman a este servicio, nunca a los DAOs directamente.
 */
public class MunicipioService {

    private final MunicipioDao municipioDao = new MunicipioDao();
    private final CupsDao      cupsDao      = new CupsDao();

    // ── Consultas ─────────────────────────────────────────────────────────────

    public List<MunicipioDto> findAll() {
        return municipioDao.findAll().stream()
                .map(m -> new MunicipioDto(m.getId(), m.getNombre()))
                .toList();
    }

    public MunicipioDto findById(Long id) {
        Municipio m = municipioDao.findById(id);
        return m == null ? null : new MunicipioDto(m.getId(), m.getNombre());
    }

    /**
     * Lista todos los CUPS pertenecientes a un municipio.
     * Incluye nombre de municipio en cada CupsDto.
     */
    public List<CupsDto> findCupsByMunicipio(Long municipioId) {
        try {
            return cupsDao.findByMunicipioId(municipioId);
        } catch (Exception e) {
            throw new RuntimeException("Error al listar CUPS del municipio", e);
        }
    }

    // ── Escritura ─────────────────────────────────────────────────────────────

    /**
     * Crea un nuevo municipio. Devuelve el DTO con el ID generado.
     */
    public MunicipioDto crear(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El campo 'nombre' es obligatorio");
        try {
            int nuevoId = municipioDao.create(nombre.trim().toUpperCase());
            return new MunicipioDto((long) nuevoId, nombre.trim().toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Error al crear municipio", e);
        }
    }

    /**
     * Renombra un municipio existente.
     */
    public MunicipioDto actualizar(int id, String nuevoNombre) {
        if (findById((long) id) == null)
            throw new IllegalArgumentException("Municipio no encontrado: " + id);
        if (nuevoNombre == null || nuevoNombre.isBlank())
            throw new IllegalArgumentException("El campo 'nombre' es obligatorio");
        try {
            municipioDao.update(id, nuevoNombre.trim().toUpperCase());
            return new MunicipioDto((long) id, nuevoNombre.trim().toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar municipio", e);
        }
    }

    /**
     * Elimina un municipio. Falla si tiene CUPS asociados (FK).
     */
    public void eliminar(int id) {
        if (findById((long) id) == null)
            throw new IllegalArgumentException("Municipio no encontrado: " + id);
        try {
            municipioDao.delete(id);
        } catch (Exception e) {
            throw new RuntimeException(
                    "No se puede eliminar: el municipio tiene CUPS asociados. " +
                            "Elimínalos primero con DELETE /api/cups/{codigo}", e);
        }
    }
}
