package com.dalila.dao;

import com.dalila.db.Db;
import com.dalila.dto.RegistroDTO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RegistroDao {

    public List<RegistroDTO> findAll() {
        List<RegistroDTO> lista = new ArrayList<>();

        String sql = """
                SELECT
                    c.id,
                    m.nombre AS municipio,
                    cu.codigo AS cups,
                    cu.direccion AS direccion,
                    c.fecha,
                    c.consumo
                FROM consumo c
                JOIN cups cu ON c.cups_codigo = cu.codigo
                JOIN municipio m ON cu.municipio_id = m.id
                ORDER BY c.fecha DESC
                """;

        try (
                Connection conn = Db.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                RegistroDTO dto = new RegistroDTO(
                        rs.getInt("id"),
                        rs.getString("municipio"),
                        rs.getString("cups"),
                        rs.getString("direccion"),
                        rs.getString("fecha"),
                        rs.getDouble("consumo")
                );
                lista.add(dto);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al obtener los registros desde la base de datos", e);
        }

        return lista;
    }

    public void insert(RegistroDTO dto) {
        String sql = """
                INSERT INTO consumo (cups_codigo, fecha, consumo)
                VALUES (?, ?, ?)
                """;

        try (
                Connection conn = Db.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, dto.getCups());
            ps.setDate(2, Date.valueOf(dto.getFecha()));
            ps.setDouble(3, dto.getConsumo());
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Error al insertar el registro", e);
        }
    }

    public void update(int id, RegistroDTO dto) {
        String sql = """
                UPDATE consumo
                SET cups_codigo = ?, fecha = ?, consumo = ?
                WHERE id = ?
                """;

        try (
                Connection conn = Db.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, dto.getCups());
            ps.setDate(2, Date.valueOf(dto.getFecha()));
            ps.setDouble(3, dto.getConsumo());
            ps.setInt(4, id);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el registro", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM consumo WHERE id = ?";

        try (
                Connection conn = Db.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar el registro", e);
        }
    }
}