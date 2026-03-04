package com.dalila;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class ImportJson {

    private static final String URL =
            "jdbc:mysql://localhost:3306/consumo_energetico?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "consumo_app";
    private static final String PASS = "consumo123";

    // Archivos con guiones, dentro de src/main/resources/data/
    private static final String[] FILES = {
            "data/consumo-energetico-2022.json",
            "data/consumo-energetico-2023.json",
            "data/consumo-energetico-2024.json",
            "data/consumo-energetico-2025.json",
            "data/consumo-energetico-2026.json"
    };

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            con.setAutoCommit(false);

            PreparedStatement insMunicipio = con.prepareStatement(
                    "INSERT IGNORE INTO municipio(nombre) VALUES (?)");
            PreparedStatement selMunicipio = con.prepareStatement(
                    "SELECT id FROM municipio WHERE nombre=?");

            PreparedStatement insDistribuidor = con.prepareStatement(
                    "INSERT IGNORE INTO distribuidor(nombre) VALUES (?)");
            PreparedStatement selDistribuidor = con.prepareStatement(
                    "SELECT id FROM distribuidor WHERE nombre=?");

            PreparedStatement upsertCups = con.prepareStatement("""
                    INSERT INTO cups(codigo, direccion, codigo_postal, municipio_id, distribuidor_id)
                    VALUES(?,?,?,?,?)
                    ON DUPLICATE KEY UPDATE
                      direccion=VALUES(direccion),
                      codigo_postal=VALUES(codigo_postal),
                      municipio_id=VALUES(municipio_id),
                      distribuidor_id=VALUES(distribuidor_id)
                    """);

            // Si tienes UNIQUE(cups_codigo, fecha), esto evita duplicados al reimportar
            PreparedStatement insConsumo = con.prepareStatement(
                    "INSERT IGNORE INTO consumo(cups_codigo, fecha, consumo) VALUES (?,?,?)"
            );

            int totalInsertConsumo = 0;
            int totalProcesados = 0;

            for (String resourcePath : FILES) {
                System.out.println("📥 Importando: " + resourcePath);

                Root root;
                try (InputStream is = ImportJson.class.getClassLoader().getResourceAsStream(resourcePath)) {
                    if (is == null) {
                        System.out.println("⚠️ No encontrado en resources: " + resourcePath);
                        continue;
                    }
                    root = mapper.readValue(is, Root.class);
                }

                int batch = 0;

                for (Municipio m : root.municipios) {
                    int municipioId = getOrCreateId(insMunicipio, selMunicipio, m.cups_municipio);

                    for (Cups c : m.cups) {
                        int distribuidorId = getOrCreateId(insDistribuidor, selDistribuidor, c.cups_distribuidor);

                        // upsert cups
                        upsertCups.setString(1, c.cups_codigo);
                        upsertCups.setString(2, c.cups_direccion);
                        if (c.cups_codigo_postal == null) upsertCups.setNull(3, Types.INTEGER);
                        else upsertCups.setInt(3, c.cups_codigo_postal);
                        upsertCups.setInt(4, municipioId);
                        upsertCups.setInt(5, distribuidorId);
                        upsertCups.executeUpdate();

                        // consumos
                        if (c.consumos != null) {
                            for (Consumo co : c.consumos) {
                                totalProcesados++;

                                LocalDate date = LocalDate.parse(co.fecha);
                                BigDecimal val = (co.consumo == null) ? BigDecimal.ZERO : co.consumo;

                                insConsumo.setString(1, c.cups_codigo);
                                insConsumo.setDate(2, Date.valueOf(date));
                                insConsumo.setBigDecimal(3, val);
                                insConsumo.addBatch();

                                batch++;
                                if (batch % 2000 == 0) {
                                    int[] res = insConsumo.executeBatch();
                                    totalInsertConsumo += countInserted(res);
                                    con.commit();
                                }
                            }
                        }
                    }
                }

                int[] res = insConsumo.executeBatch();
                totalInsertConsumo += countInserted(res);
                con.commit();

                System.out.println("✅ OK: " + resourcePath);
            }

            System.out.println("🎉 Import finalizado.");
            System.out.println("   Consumos procesados: " + totalProcesados);
            System.out.println("   Consumos insertados (aprox): " + totalInsertConsumo);
        }
    }

    // Cuenta inserts (INSERT IGNORE devuelve 1 si insertó, 0 si ignoró)
    private static int countInserted(int[] batchResult) {
        int c = 0;
        if (batchResult == null) return 0;
        for (int r : batchResult) {
            if (r > 0) c++;
        }
        return c;
    }

    private static int getOrCreateId(PreparedStatement insertIgnore, PreparedStatement selectId, String name) throws SQLException {
        insertIgnore.setString(1, name);
        insertIgnore.executeUpdate();

        selectId.setString(1, name);
        try (ResultSet rs = selectId.executeQuery()) {
            if (!rs.next()) throw new SQLException("No pude recuperar ID para: " + name);
            return rs.getInt(1);
        }
    }

    // ===== DTOs JSON =====
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Root { public List<Municipio> municipios; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Municipio {
        public String cups_municipio;
        public List<Cups> cups;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Cups {
        public String cups_codigo;
        public String cups_direccion;
        public Integer cups_codigo_postal;
        public String cups_distribuidor;
        public List<Consumo> consumos;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Consumo {
        public String fecha;
        public BigDecimal consumo;
    }
}