package com.dalila.dao;

import com.dalila.db.Db;
import com.dalila.entity.Municipio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MunicipioDao {

    public List<Municipio> findAll(int limit) throws SQLException {
        String sql = "SELECT id, nombre FROM municipio ORDER BY nombre LIMIT ?";
        List<Municipio> out = new ArrayList<>();

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        }
        return out;
    }

    public Municipio findById(int id) throws SQLException {
        String sql = "SELECT id, nombre FROM municipio WHERE id = ?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public int create(String nombre) throws SQLException {
        String sql = "INSERT INTO municipio(nombre) VALUES (?)";

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No se generó ID");
                return keys.getInt(1);
            }
        }
    }

    public boolean update(int id, String nuevoNombre) throws SQLException {
        String sql = "UPDATE municipio SET nombre = ? WHERE id = ?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoNombre);
            ps.setInt(2, id);

            return ps.executeUpdate() == 1;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM municipio WHERE id = ?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private Municipio map(ResultSet rs) throws SQLException {
        Municipio m = new Municipio();
        m.setId(rs.getInt("id"));
        m.setNombre(rs.getString("nombre"));
        return m;
    }

    public List<Municipio> findAll() {
        List<Municipio> lista = new ArrayList<>();

        String sql = "SELECT id, nombre FROM municipio ORDER BY nombre";

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Municipio municipio = new Municipio();
                municipio.setId(rs.getInt("id"));
                municipio.setNombre(rs.getString("nombre"));
                lista.add(municipio);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo municipios", e);
        }

        return lista;
    }
}