package com.dalila.service;
import com.dalila.dto.RegistroDTO;
import java.util.List;
import com.dalila.dao.RegistroDao;
public class RegistroService {
    private RegistroDao registroDao = new RegistroDao();

    public List<RegistroDTO> listarTodos() {
        return registroDao.findAll();
    }

    public List<RegistroDTO> findFiltered(String municipio, String cups, String direccion,
                                          String fechaMin, String fechaMax,
                                          Double consumoMin, Double consumoMax) {

        // Llamamos al método real de tu base de datos
        return registroDao.findFiltered(municipio, cups, direccion, fechaMin, fechaMax, consumoMin, consumoMax);
    }
}
