package com.dalila.service;

import com.dalila.dao.MunicipioDao;
import com.dalila.dto.CupsDto;
import com.dalila.dto.MunicipioDto;
import com.dalila.entity.Cups;
import com.dalila.entity.Municipio;

import java.util.List;

public class MunicipioService {

    private final MunicipioDao municipioDao = new MunicipioDao();

    public List<MunicipioDto> findAll() {
        List<Municipio> municipios = municipioDao.findAll();

        return municipios.stream()
                .map(m -> new MunicipioDto(
                        m.getId(),
                        m.getNombre()
                ))
                .toList();
    }

    public MunicipioDto findById(Long id) {
        Municipio municipio = municipioDao.findById(id);

        if (municipio == null) {
            return null;
        }

        return new MunicipioDto(
                municipio.getId(),
                municipio.getNombre()
        );
    }

    public List<CupsDto> findCupsByMunicipio(Long idMunicipio) {
        List<Cups> cups = municipioDao.findCupsByMunicipio(idMunicipio);

        return cups.stream()
                .map(c -> new CupsDto(
                        c.getCodigo(),
                        c.getDireccion(),
                        c.getCodigoPostal()
                ))
                .toList();
    }
}