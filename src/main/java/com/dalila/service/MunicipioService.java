package com.dalila.service;

import com.dalila.dao.MunicipioDao;
import com.dalila.dto.MunicipioDto;
import com.dalila.entity.Municipio;

import java.util.List;

public class MunicipioService {

        public List<MunicipioDto> findAll() {

            final MunicipioDao municipioDao = new MunicipioDao();
            List<Municipio> municipios = municipioDao.findAll();

            return municipios.stream()
                    .map(m -> new MunicipioDto(
                            m.getId(),
                            m.getNombre()
                    ))
                    .toList();
        }
}
