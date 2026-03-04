package com.dalila.dao;

import com.dalila.db.Db;
import com.dalila.entity.Distribuidor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DistribuidorDao {

    public List<Distribuidor> findAll() throws SQLException {

        String sql = "SELECT id, nombre FROM distribuidor ORDER BY nombre";

        List<Distribuidor> lista = new ArrayList<>();

        try (
                Connection con = Db.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {
                lista.add(map(rs));
            }

        }

        return lista;
    }

    public Distribuidor findById(int id) throws SQLException {

        String sql = "SELECT id, nombre FROM distribuidor WHERE id = ?";

        try (
                Connection con = Db.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return map(rs);
                }

            }

        }

        return null;
    }

    private Distribuidor map(ResultSet rs) throws SQLException {

        Distribuidor d = new Distribuidor();

        d.setId(rs.getInt("id"));
        d.setNombre(rs.getString("nombre"));

        return d;
    }
}