package com.dalila.dao;

import com.dalila.db.Db;
import com.dalila.dto.RegistroDTO;

import java.sql.Connection;
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
}