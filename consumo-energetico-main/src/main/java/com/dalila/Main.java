package com.dalila;

import com.dalila.dao.CupsDao;
import com.dalila.entity.Cups;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {
        CupsDao dao = new CupsDao();

        // 1) Listar 5 cups
        List<Cups> lista = dao.findAll(5);
        for (Cups c : lista) {
            System.out.println(c.getCodigo() + " | " + c.getCodigoPostal());
        }

        // 2) Filtrar por municipio (prueba con "ADEJE")
        List<Cups> adeje = dao.findByMunicipioNombre("ADEJE", 5);
        System.out.println("CUPS en ADEJE: " + adeje.size());
    }
}