package com.dalila.service;

import com.dalila.dto.ConsumoAnualDto;
import com.dalila.dto.DetalleEstadisticoAnualDTO;
import com.dalila.dto.RegistroDTO;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de RegistroService usando la BD real.
 * No usa Mockito — compatible con Java 25.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RegistroServiceTest {

    private static RegistroService service;
    private static final String CUPS_EXISTENTE = "ES0031601106517001TW0F";

    @BeforeAll
    static void setUp() {
        service = new RegistroService();
    }

    @Test @Order(1)
    @DisplayName("findAll devuelve lista no vacía")
    void findAllDevuelveListaNoVacia() {
        assertFalse(service.findAll().isEmpty());
    }

    @Test @Order(2)
    @DisplayName("findById devuelve null para ID inexistente")
    void findByIdDevuelveNullParaIdInexistente() {
        assertNull(service.findById(Integer.MAX_VALUE));
    }

    @Test @Order(3)
    @DisplayName("findById devuelve el registro correcto")
    void findByIdDevuelveRegistroCorrecto() {
        int id = service.findAll().get(0).getId();
        assertEquals(id, service.findById(id).getId());
    }

    @Test @Order(4)
    @DisplayName("crear lanza excepción si falta el CUPS")
    void crearLanzaExcepcionSiFaltaCups() {
        RegistroDTO dto = new RegistroDTO();
        dto.setFecha("2099-01-01");
        dto.setConsumo(1.0);
        assertThrows(IllegalArgumentException.class, () -> service.crear(dto));
    }

    @Test @Order(5)
    @DisplayName("crear lanza excepción si falta la fecha")
    void crearLanzaExcepcionSiFaltaFecha() {
        RegistroDTO dto = new RegistroDTO();
        dto.setCups(CUPS_EXISTENTE);
        dto.setConsumo(1.0);
        assertThrows(IllegalArgumentException.class, () -> service.crear(dto));
    }

    @Test @Order(6)
    @DisplayName("crear lanza excepción si falta el consumo")
    void crearLanzaExcepcionSiFaltaConsumo() {
        RegistroDTO dto = new RegistroDTO();
        dto.setCups(CUPS_EXISTENTE);
        dto.setFecha("2099-01-01");
        assertThrows(IllegalArgumentException.class, () -> service.crear(dto));
    }

    @Test @Order(7)
    @DisplayName("crear inserta y devuelve ID real")
    void crearInsertaYDevuelveIdReal() {
        RegistroDTO dto = new RegistroDTO();
        dto.setCups(CUPS_EXISTENTE);
        dto.setFecha("2099-06-01");
        dto.setConsumo(77.77);
        RegistroDTO creado = service.crear(dto);
        assertTrue(creado.getId() > 0);
        service.eliminar(creado.getId());
    }

    @Test @Order(8)
    @DisplayName("actualizar lanza excepción si no existe")
    void actualizarLanzaExcepcionSiNoExiste() {
        RegistroDTO dto = new RegistroDTO();
        dto.setCups(CUPS_EXISTENTE);
        dto.setFecha("2099-01-01");
        dto.setConsumo(1.0);
        assertThrows(IllegalArgumentException.class,
                () -> service.actualizar(Integer.MAX_VALUE, dto));
    }

    @Test @Order(9)
    @DisplayName("actualizar modifica el consumo")
    void actualizarModificaConsumo() {
        RegistroDTO nuevo = new RegistroDTO();
        nuevo.setCups(CUPS_EXISTENTE);
        nuevo.setFecha("2099-07-01");
        nuevo.setConsumo(10.0);
        RegistroDTO creado = service.crear(nuevo);

        RegistroDTO mod = new RegistroDTO();
        mod.setCups(CUPS_EXISTENTE);
        mod.setFecha("2099-07-01");
        mod.setConsumo(999.99);

        RegistroDTO resultado = service.actualizar(creado.getId(), mod);
        assertEquals(999.99, resultado.getConsumo(), 0.001);
        service.eliminar(creado.getId());
    }

    @Test @Order(10)
    @DisplayName("eliminar lanza excepción si no existe")
    void eliminarLanzaExcepcionSiNoExiste() {
        assertThrows(IllegalArgumentException.class,
                () -> service.eliminar(Integer.MAX_VALUE));
    }

    @Test @Order(11)
    @DisplayName("getRegistrosPorAnio filtra por año")
    void getRegistrosPorAnioFiltra() {
        List<RegistroDTO> res = service.getRegistrosPorAnio(2023);
        assertFalse(res.isEmpty());
        assertTrue(res.stream().allMatch(r -> r.getFecha().startsWith("2023")));
    }

    @Test @Order(12)
    @DisplayName("getResumenGlobal devuelve datos reales")
    void getResumenGlobalDevuelveDatos() {
        var resumen = service.getResumenGlobal();
        assertNotNull(resumen.getDiaMayorConsumo());
        assertNotNull(resumen.getAnioMasConsumo());
    }

    @Test @Order(13)
    @DisplayName("getResumenAnual devuelve lista con años")
    void getResumenAnualDevuelveLista() {
        List<ConsumoAnualDto> lista = service.getResumenAnual();
        assertFalse(lista.isEmpty());
    }

    @Test @Order(14)
    @DisplayName("getAnalisisPorAnio devuelve null para año sin datos")
    void getAnalisisPorAnioNullSiNoHayDatos() {
        assertNull(service.getAnalisisPorAnio(1800));
    }

    @Test @Order(15)
    @DisplayName("getAnalisisPorAnio devuelve análisis de 2023")
    void getAnalisisPorAnioDevuelveAnalisis() {
        DetalleEstadisticoAnualDTO res = service.getAnalisisPorAnio(2023);
        assertNotNull(res);
        assertEquals(2023, res.getAnio());
        assertTrue(res.getConsumoTotalAnual() > 0);
    }
}