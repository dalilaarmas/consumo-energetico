package com.dalila;
import org.flywaydb.core.Flyway;
public class Migrate {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/consumo_energetico?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "consumo_app";
        String password = "consumo123";

        Flyway flyway = Flyway.configure()
                .dataSource(url, user, password)
                .locations("classpath:db/migration")
                .load();

        flyway.migrate();

        System.out.println("Migración completada. Tablas creadas.");
    }

}
