package com.dalila.service;

import com.dalila.dao.CupsDao;
import com.dalila.dto.CupsDto;
import com.dalila.entity.Cups;

import java.util.List;

/**
 * Servicio de CUPS. Contiene toda la lógica de negocio relacionada con CUPS.
 * Los Resources solo llaman a este servicio, nunca al DAO directamente.
 */
public class CupsService {

    private final CupsDao cupsDao = new CupsDao();

    // ── Consultas ─────────────────────────────────────────────────────────────

    /**
     * Lista CUPS con filtro opcional por municipio y límite de resultados.
     */
    public List<CupsDto> findAll(String municipio, int limit) {
        try {
            return cupsDao.findAll(municipio, limit);
        } catch (Exception e) {
            throw new RuntimeException("Error al listar CUPS", e);
        }
    }

    /**
     * Busca un CUPS por código exacto. Devuelve null si no existe.
     */
    public CupsDto findByCodigo(String codigo) {
        try {
            return cupsDao.findByCodigo(codigo);
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar CUPS por código", e);
        }
    }

    /**
     * Lista todos los CUPS de un municipio.
     */
    public List<CupsDto> findByMunicipioId(long municipioId) {
        try {
            return cupsDao.findByMunicipioId(municipioId);
        } catch (Exception e) {
            throw new RuntimeException("Error al listar CUPS del municipio", e);
        }
    }

    // ── Escritura ─────────────────────────────────────────────────────────────

    /**
     * Crea un nuevo CUPS. Lanza IllegalArgumentException si ya existe.
     */
    public CupsDto crear(Cups cups) {
        validarCups(cups);

        if (findByCodigo(cups.getCodigo()) != null) {
            throw new IllegalArgumentException("Ya existe un CUPS con el código: " + cups.getCodigo());
        }
        try {
            cupsDao.create(cups);
            return findByCodigo(cups.getCodigo());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al crear el CUPS", e);
        }
    }

    /**
     * Actualiza un CUPS existente. Lanza IllegalArgumentException si no existe.
     */
    public CupsDto actualizar(String codigo, Cups cups) {
        if (findByCodigo(codigo) == null) {
            throw new IllegalArgumentException("CUPS no encontrado: " + codigo);
        }
        cups.setCodigo(codigo);
        try {
            cupsDao.update(cups);
            return findByCodigo(codigo);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el CUPS", e);
        }
    }

    /**
     * Elimina un CUPS. Lanza IllegalArgumentException si no existe.
     * Lanza RuntimeException si tiene consumos asociados (FK).
     */
    public void eliminar(String codigo) {
        if (findByCodigo(codigo) == null) {
            throw new IllegalArgumentException("CUPS no encontrado: " + codigo);
        }
        try {
            cupsDao.delete(codigo);
        } catch (Exception e) {
            throw new RuntimeException(
                    "No se puede eliminar el CUPS: tiene registros de consumo asociados. " +
                            "Elimínalos primero con DELETE /api/registros/{id}", e);
        }
    }

    // ── Validaciones ──────────────────────────────────────────────────────────

    private void validarCups(Cups cups) {
        if (cups.getCodigo() == null || cups.getCodigo().isBlank())
            throw new IllegalArgumentException("El campo 'codigo' es obligatorio");
        if (cups.getDireccion() == null || cups.getDireccion().isBlank())
            throw new IllegalArgumentException("El campo 'direccion' es obligatorio");
        if (cups.getMunicipioId() == 0)
            throw new IllegalArgumentException(
                    "El campo 'municipioId' es obligatorio. " +
                            "Consulta GET /api/municipios para ver los IDs disponibles");
    }
}