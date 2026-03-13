package com.dalila.service;

import com.dalila.dao.CupsDao;
import com.dalila.dto.CupsDto;
import com.dalila.entity.Cups;

public class CupsService {

    private final CupsDao cupsDao = new CupsDao();

    public CupsDto findByCodigo(String codigo) {
        Cups c = cupsDao.findByCodigo(codigo);

        if (c == null) {
            return null;
        }

        return new CupsDto(
                c.getCodigo(),
                c.getDireccion(),
                c.getCodigoPostal()
        );
    }
}