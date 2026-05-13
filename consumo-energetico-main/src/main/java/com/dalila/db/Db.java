package com.dalila.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Db {
    private static final String URL =
            "jdbc:mysql://localhost:3306/consumo_energetico?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "consumo_app";
    private static final String PASS = "consumo123";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver MySQL cargado correctamente");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se pudo cargar el driver de MySQL", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}