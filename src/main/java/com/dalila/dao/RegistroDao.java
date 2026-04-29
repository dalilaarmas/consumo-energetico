package com.dalila.dao;

import com.dalila.db.Db;
import com.dalila.dto.ConsumoAnualDto;
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

    // --- MÉTODOS CRUD RESTAURADOS ---

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

    // --- MÉTODOS DEL BUSCADOR Y PDF ---

    public List<RegistroDTO> findFiltered(String municipio, String cups, String direccion,
                                          String fechaDesde, String fechaHasta,
                                          Double consumoMin, Double consumoMax) {
        List<RegistroDTO> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
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
        WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (municipio != null && !municipio.trim().isEmpty()) {
            sql.append(" AND LOWER(m.nombre) LIKE ? ");
            params.add("%" + municipio.toLowerCase() + "%");
        }

        if (cups != null && !cups.trim().isEmpty()) {
            sql.append(" AND LOWER(cu.codigo) LIKE ? ");
            params.add("%" + cups.toLowerCase() + "%");
        }

        if (direccion != null && !direccion.trim().isEmpty()) {
            sql.append(" AND LOWER(cu.direccion) LIKE ? ");
            params.add("%" + direccion.toLowerCase() + "%");
        }

        Date fechaDesdeSql = parseFechaMin(fechaDesde);
        Date fechaHastaSql = parseFechaMax(fechaHasta);

        if (fechaDesdeSql != null) {
            sql.append(" AND c.fecha >= ? ");
            params.add(fechaDesdeSql);
        }

        if (fechaHastaSql != null) {
            sql.append(" AND c.fecha <= ? ");
            params.add(fechaHastaSql);
        }

        if (consumoMin != null) {
            sql.append(" AND c.consumo >= ? ");
            params.add(consumoMin);
        }

        if (consumoMax != null) {
            sql.append(" AND c.consumo <= ? ");
            params.add(consumoMax);
        }

        sql.append(" ORDER BY c.fecha DESC ");

        try (
                Connection conn = Db.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())
        ) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
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
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    private Date parseFechaMin(String valor) {
        if (valor == null || valor.trim().isEmpty()) return null;

        valor = valor.trim();

        if (valor.matches("^\\d{4}$")) {
            return Date.valueOf(valor + "-01-01");
        }

        if (valor.matches("^\\d{4}-\\d{2}$")) {
            return Date.valueOf(valor + "-01");
        }

        if (valor.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return Date.valueOf(valor);
        }

        return null;
    }

    public RegistroDTO findById(int id) {
        RegistroDTO registro = null;

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
                WHERE c.id = ?
                """;

        try (
                Connection conn = Db.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, id); // Sustituimos la interrogación por el ID

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { // Si encuentra un resultado, creamos el objeto
                    registro = new RegistroDTO(
                            rs.getInt("id"),
                            rs.getString("municipio"),
                            rs.getString("cups"),
                            rs.getString("direccion"),
                            rs.getString("fecha"),
                            rs.getDouble("consumo")
                    );
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al obtener el registro por ID", e);
        }

        return registro; // Devolverá el registro, o null si no encontró ese ID
    }

    private Date parseFechaMax(String valor) {
        if (valor == null || valor.trim().isEmpty()) return null;

        valor = valor.trim();

        if (valor.matches("^\\d{4}$")) {
            return Date.valueOf(valor + "-12-31");
        }

        if (valor.matches("^\\d{4}-\\d{2}$")) {
            int anio = Integer.parseInt(valor.substring(0, 4));
            int mes = Integer.parseInt(valor.substring(5, 7));
            int dia = 31;
            if (mes == 4 || mes == 6 || mes == 9 || mes == 11) {
                dia = 30;
            } else if (mes == 2) {
                boolean bisiesto = (anio % 4 == 0 && (anio % 100 != 0 || anio % 400 == 0));
                dia = bisiesto ? 29 : 28;
            }
            return Date.valueOf(valor + "-" + dia);
        }

        if (valor.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return Date.valueOf(valor);
        }

        return null;
    }

    // metodo para el Resumen General
    public Double obtenerConsumoTotalGlobal() {
        // Le decimos a SQL que sume toda la columna consumo
        String sql = "SELECT SUM(consumo) AS total FROM consumo";

        try (
                Connection conn = Db.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al calcular el total global", e);
        }
        return 0.0;
    }

    // metodo para el Resumen por Años
    public List<ConsumoAnualDto> obtenerResumenAnual() {
        List<ConsumoAnualDto> lista = new ArrayList<>();

        // Magia SQL: Extraemos el año de la fecha, sumamos el consumo,
        // y agrupamos los resultados por ese año.
        String sql = """
                SELECT YEAR(fecha) as anio, SUM(consumo) as total 
                FROM consumo 
                GROUP BY YEAR(fecha) 
                ORDER BY YEAR(fecha) DESC
                """;

        try (
                Connection conn = Db.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                // Por cada año que encuentre, creamos nuestro DTO y lo guardamos
                lista.add(new ConsumoAnualDto(
                        rs.getInt("anio"),
                        rs.getDouble("total")
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al calcular el resumen anual", e);
        }
        return lista;
    }


}