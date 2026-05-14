package com.dalila.dao;

import com.dalila.db.Db;
import com.dalila.dto.CupsDto;
import com.dalila.entity.Cups;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de CUPS. Solo acceso a datos, sin lógica de negocio.
 * Devuelve entidades (Cups) o DTOs cuando el JOIN aporta valor directo (findByCodigo).
 */
public class CupsDao {

    // ── Lectura ──────────────────────────────────────────────────────────────

    /**
     * Lista CUPS con JOIN a municipio para incluir el nombre.
     * Soporta búsqueda parcial por municipio y límite de resultados.
     */
    public List<CupsDto> findAll(String municipio, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT c.codigo, c.direccion, c.codigo_postal,
                       c.municipio_id, m.nombre AS municipio, c.distribuidor_id
                FROM cups c
                LEFT JOIN municipio m ON m.id = c.municipio_id
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (municipio != null && !municipio.isBlank()) {
            sql.append(" AND LOWER(m.nombre) LIKE ? ");
            params.add("%" + municipio.toLowerCase() + "%");
        }

        sql.append(" ORDER BY c.codigo LIMIT ?");
        params.add(limit);

        List<CupsDto> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapDto(rs));
            }
        }
        return out;
    }

    /**
     * Busca un CUPS por código exacto. Incluye nombre del municipio.
     * Devuelve null si no existe.
     */
    public CupsDto findByCodigo(String codigo) throws SQLException {
        String sql = """
                SELECT c.codigo, c.direccion, c.codigo_postal,
                       c.municipio_id, m.nombre AS municipio, c.distribuidor_id
                FROM cups c
                LEFT JOIN municipio m ON m.id = c.municipio_id
                WHERE c.codigo = ?
                """;

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapDto(rs);
            }
        }
        return null;
    }

    /**
     * Lista todos los CUPS de un municipio dado su ID.
     */
    public List<CupsDto> findByMunicipioId(long municipioId) throws SQLException {
        String sql = """
                SELECT c.codigo, c.direccion, c.codigo_postal,
                       c.municipio_id, m.nombre AS municipio, c.distribuidor_id
                FROM cups c
                LEFT JOIN municipio m ON m.id = c.municipio_id
                WHERE c.municipio_id = ?
                ORDER BY c.codigo
                """;

        List<CupsDto> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, municipioId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapDto(rs));
            }
        }
        return out;
    }

    // ── Escritura ─────────────────────────────────────────────────────────────

    public void create(Cups c) throws SQLException {
        String sql = """
                INSERT INTO cups(codigo, direccion, codigo_postal, municipio_id, distribuidor_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getCodigo());
            ps.setString(2, c.getDireccion());
            if (c.getCodigoPostal() == null) ps.setNull(3, Types.INTEGER);
            else                             ps.setInt(3, c.getCodigoPostal());
            ps.setInt(4, c.getMunicipioId());
            ps.setInt(5, c.getDistribuidorId());
            ps.executeUpdate();
        }
    }

    public void update(Cups c) throws SQLException {
        String sql = """
                UPDATE cups
                SET direccion = ?, codigo_postal = ?, municipio_id = ?, distribuidor_id = ?
                WHERE codigo = ?
                """;

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getDireccion());
            if (c.getCodigoPostal() == null) ps.setNull(2, Types.INTEGER);
            else                             ps.setInt(2, c.getCodigoPostal());
            ps.setInt(3, c.getMunicipioId());
            ps.setInt(4, c.getDistribuidorId());
            ps.setString(5, c.getCodigo());
            ps.executeUpdate();
        }
    }

    public void delete(String codigo) throws SQLException {
        String sql = "DELETE FROM cups WHERE codigo = ?";

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);
            ps.executeUpdate();
        }
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private CupsDto mapDto(ResultSet rs) throws SQLException {
        int cp = rs.getInt("codigo_postal");
        return new CupsDto(
                rs.getString("codigo"),
                rs.getString("direccion"),
                rs.wasNull() ? null : cp,
                rs.getInt("municipio_id"),
                rs.getString("municipio"),
                rs.getInt("distribuidor_id")
        );
    }
}