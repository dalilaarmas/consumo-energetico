package com.dalila.dao;

import com.dalila.db.Db;
import com.dalila.entity.Consumo;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ConsumoDao {

    public List<Consumo> findByCupsAndRange(String cupsCodigo, LocalDate desde, LocalDate hasta, int limit) throws SQLException {
        String sql = """
                SELECT id, cups_codigo, fecha, consumo
                FROM consumo
                WHERE cups_codigo = ?
                  AND fecha >= ?
                  AND fecha <= ?
                ORDER BY fecha
                LIMIT ?
                """;

        List<Consumo> out = new ArrayList<>();

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cupsCodigo);
            ps.setDate(2, Date.valueOf(desde));
            ps.setDate(3, Date.valueOf(hasta));
            ps.setInt(4, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    // Consulta “pro”: consumos por municipio y rango (JOIN municipio -> cups -> consumo)
    public List<Consumo> findByMunicipioAndRange(String municipioNombre, LocalDate desde, LocalDate hasta, int limit) throws SQLException {
        String sql = """
                SELECT co.id, co.cups_codigo, co.fecha, co.consumo
                FROM consumo co
                JOIN cups c ON c.codigo = co.cups_codigo
                JOIN municipio m ON m.id = c.municipio_id
                WHERE m.nombre = ?
                  AND co.fecha >= ?
                  AND co.fecha <= ?
                ORDER BY co.fecha
                LIMIT ?
                """;

        List<Consumo> out = new ArrayList<>();

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, municipioNombre);
            ps.setDate(2, Date.valueOf(desde));
            ps.setDate(3, Date.valueOf(hasta));
            ps.setInt(4, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    // Crear un consumo (si tu UNIQUE(cups_codigo, fecha) está puesto, evita duplicados)
    public boolean create(String cupsCodigo, LocalDate fecha, BigDecimal consumo) throws SQLException {
        String sql = "INSERT INTO consumo(cups_codigo, fecha, consumo) VALUES (?,?,?)";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cupsCodigo);
            ps.setDate(2, Date.valueOf(fecha));
            ps.setBigDecimal(3, consumo);

            return ps.executeUpdate() == 1;
        }
    }

    private Consumo map(ResultSet rs) throws SQLException {
        Consumo c = new Consumo();
        c.setId(rs.getLong("id"));
        c.setCupsCodigo(rs.getString("cups_codigo"));
        c.setFecha(rs.getDate("fecha").toLocalDate());
        c.setConsumo(rs.getBigDecimal("consumo"));
        return c;
    }
}