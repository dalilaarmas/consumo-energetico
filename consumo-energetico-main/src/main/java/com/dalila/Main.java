package com.dalila;

import com.dalila.dao.CupsDao;
import com.dalila.dto.CupsDto;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {
        CupsDao dao = new CupsDao();

        // 1) Listar 5 cups
        List<CupsDto> lista = dao.findAll(null, 5);
        for (CupsDto c : lista) {
            System.out.println(c.getCodigo() + " | " + c.getMunicipio() + " | " + c.getCodigoPostal());
        }

        // 2) Filtrar por municipio
        List<CupsDto> adeje = dao.findAll("ADEJE", 5);
        System.out.println("CUPS en ADEJE: " + adeje.size());
    }
}
