package com.dalila.dao;

import com.dalila.db.Db;
import com.dalila.entity.Cups;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CupsDao {

    public List<Cups> findAll(int limit) throws SQLException {
        String sql = """
                SELECT codigo, direccion, codigo_postal, municipio_id, distribuidor_id
                FROM cups
                ORDER BY codigo
                LIMIT ?
                """;

        List<Cups> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    public Cups findByCodigo(String codigo) {
        String sql = """
            SELECT codigo, direccion, codigo_postal, distribuidor_id
            FROM cups
            WHERE codigo = ?
            """;

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cups cups = new Cups();
                    cups.setCodigo(rs.getString("codigo"));
                    cups.setDireccion(rs.getString("direccion"));
                    cups.setCodigoPostal(rs.getInt("codigo_postal"));
                    cups.setDistribuidorId(rs.getInt("distribuidor_id"));
                    return cups;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error obteniendo cups por codigo", e);
        }

        return null;
    }

    // Filtrar por municipio (útil para REST: GET /cups?municipio=ADEJE)
    public List<Cups> findByMunicipioNombre(String municipioNombre, int limit) throws SQLException {
        String sql = """
                SELECT c.codigo, c.direccion, c.codigo_postal, c.municipio_id, c.distribuidor_id
                FROM cups c
                JOIN municipio m ON m.id = c.municipio_id
                WHERE m.nombre = ?
                ORDER BY c.codigo
                LIMIT ?
                """;

        List<Cups> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, municipioNombre);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    // Filtrar por distribuidor (GET /cups?distribuidor=EDISTRIBUCIÓN)
    public List<Cups> findByDistribuidorNombre(String distribuidorNombre, int limit) throws SQLException {
        String sql = """
                SELECT c.codigo, c.direccion, c.codigo_postal, c.municipio_id, c.distribuidor_id
                FROM cups c
                JOIN distribuidor d ON d.id = c.distribuidor_id
                WHERE d.nombre = ?
                ORDER BY c.codigo
                LIMIT ?
                """;

        List<Cups> out = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, distribuidorNombre);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    // CREATE (POST /cups) -> normalmente no lo usarás si tus datos vienen del JSON, pero queda profesional
    public boolean create(Cups c) throws SQLException {
        String sql = """
                INSERT INTO cups(codigo, direccion, codigo_postal, municipio_id, distribuidor_id)
                VALUES (?,?,?,?,?)
                """;

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getCodigo());
            ps.setString(2, c.getDireccion());

            if (c.getCodigoPostal() == null) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, c.getCodigoPostal());

            ps.setInt(4, c.getMunicipioId());
            ps.setInt(5, c.getDistribuidorId());

            return ps.executeUpdate() == 1;
        }
    }

    // UPDATE (PUT /cups/{codigo})
    public boolean update(Cups c) throws SQLException {
        String sql = """
                UPDATE cups
                SET direccion = ?,
                    codigo_postal = ?,
                    municipio_id = ?,
                    distribuidor_id = ?
                WHERE codigo = ?
                """;

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getDireccion());

            if (c.getCodigoPostal() == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, c.getCodigoPostal());

            ps.setInt(3, c.getMunicipioId());
            ps.setInt(4, c.getDistribuidorId());
            ps.setString(5, c.getCodigo());

            return ps.executeUpdate() == 1;
        }
    }

    // DELETE (DELETE /cups/{codigo})
    // OJO: si hay consumos asociados, por FK no te dejará borrar. Eso es correcto.
    public boolean delete(String codigo) throws SQLException {
        String sql = "DELETE FROM cups WHERE codigo = ?";

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);
            return ps.executeUpdate() == 1;
        }
    }

    private Cups map(ResultSet rs) throws SQLException {
        Cups c = new Cups();
        c.setCodigo(rs.getString("codigo"));
        c.setDireccion(rs.getString("direccion"));

        int cp = rs.getInt("codigo_postal");
        if (rs.wasNull()) c.setCodigoPostal(null);
        else c.setCodigoPostal(cp);

        c.setMunicipioId(rs.getInt("municipio_id"));
        c.setDistribuidorId(rs.getInt("distribuidor_id"));
        return c;
    }
}