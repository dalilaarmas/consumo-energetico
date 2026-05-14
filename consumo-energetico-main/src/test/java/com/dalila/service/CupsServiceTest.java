package com.dalila.service;

import com.dalila.dto.CupsDto;
import com.dalila.entity.Cups;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de CupsService usando la BD real.
 * No usa Mockito — compatible con Java 25.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CupsServiceTest {

    private static CupsService service;
    private static final String CUPS_EXISTENTE  = "ES0031601106517001TW0F";
    private static final String CUPS_TEST       = "ESTEST99999999999TEST99";
    private static final int    MUNICIPIO_ID    = 1;
    private static final int    DISTRIBUIDOR_ID = 1;

    @BeforeAll
    static void setUp() {
        service = new CupsService();
        // Limpiar si quedó de una ejecución anterior
        try { service.eliminar(CUPS_TEST); } catch (Exception ignored) {}
    }

    @Test @Order(1)
    @DisplayName("findByCodigo devuelve CUPS con municipio")
    void findByCodigoDevuelveCupsConMunicipio() {
        CupsDto resultado = service.findByCodigo(CUPS_EXISTENTE);
        assertNotNull(resultado);
        assertEquals(CUPS_EXISTENTE, resultado.getCodigo());
        assertNotNull(resultado.getMunicipio());
        assertFalse(resultado.getMunicipio().isBlank());
    }

    @Test @Order(2)
    @DisplayName("findByCodigo devuelve null para código inexistente")
    void findByCodigoDevuelveNullSiNoExiste() {
        assertNull(service.findByCodigo("CODIGO_INEXISTENTE_99999"));
    }

    @Test @Order(3)
    @DisplayName("findAll sin filtro devuelve lista no vacía")
    void findAllSinFiltroDevuelveListaNoVacia() {
        List<CupsDto> lista = service.findAll(null, 10);
        assertFalse(lista.isEmpty());
        assertTrue(lista.size() <= 10);
    }

    @Test @Order(4)
    @DisplayName("findAll con municipio filtra correctamente")
    void findAllConMunicipioFiltrado() {
        List<CupsDto> lista = service.findAll("ADEJE", 50);
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().allMatch(c ->
                c.getMunicipio() != null &&
                        c.getMunicipio().toUpperCase().contains("ADEJE")
        ));
    }

    @Test @Order(5)
    @DisplayName("crear lanza excepción si código vacío")
    void crearLanzaExcepcionSiCodigoVacio() {
        Cups cups = new Cups("", "Dir", 38001, MUNICIPIO_ID, DISTRIBUIDOR_ID);
        assertThrows(IllegalArgumentException.class, () -> service.crear(cups));
    }

    @Test @Order(6)
    @DisplayName("crear lanza excepción si dirección vacía")
    void crearLanzaExcepcionSiDireccionVacia() {
        Cups cups = new Cups("ES999", "", 38001, MUNICIPIO_ID, DISTRIBUIDOR_ID);
        assertThrows(IllegalArgumentException.class, () -> service.crear(cups));
    }

    @Test @Order(7)
    @DisplayName("crear lanza excepción si municipioId es 0")
    void crearLanzaExcepcionSiMunicipioIdCero() {
        Cups cups = new Cups("ES999", "Dir", 38001, 0, DISTRIBUIDOR_ID);
        assertThrows(IllegalArgumentException.class, () -> service.crear(cups));
    }

    @Test @Order(8)
    @DisplayName("crear inserta correctamente y devuelve DTO con municipio")
    void crearInsertaCorrectamente() {
        Cups cups = new Cups(CUPS_TEST, "Dirección Test", 38099, MUNICIPIO_ID, DISTRIBUIDOR_ID);
        CupsDto creado = service.crear(cups);

        assertNotNull(creado);
        assertEquals(CUPS_TEST, creado.getCodigo());
        assertEquals("Dirección Test", creado.getDireccion());
        assertNotNull(creado.getMunicipio());
    }

    @Test @Order(9)
    @DisplayName("crear lanza excepción si el CUPS ya existe")
    void crearLanzaExcepcionSiYaExiste() {
        Cups cups = new Cups(CUPS_TEST, "Dir 2", 38001, MUNICIPIO_ID, DISTRIBUIDOR_ID);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> service.crear(cups)
        );
        assertTrue(ex.getMessage().contains(CUPS_TEST));
    }

    @Test @Order(10)
    @DisplayName("actualizar modifica la dirección")
    void actualizarModificaDireccion() {
        Cups mod = new Cups(CUPS_TEST, "Dirección Modificada", 38001, MUNICIPIO_ID, DISTRIBUIDOR_ID);
        CupsDto resultado = service.actualizar(CUPS_TEST, mod);
        assertEquals("Dirección Modificada", resultado.getDireccion());
    }

    @Test @Order(11)
    @DisplayName("actualizar lanza excepción si no existe")
    void actualizarLanzaExcepcionSiNoExiste() {
        Cups cups = new Cups("NOEXISTE", "Dir", 38001, MUNICIPIO_ID, DISTRIBUIDOR_ID);
        assertThrows(IllegalArgumentException.class,
                () -> service.actualizar("NOEXISTE", cups));
    }

    @Test @Order(12)
    @DisplayName("eliminar borra el CUPS correctamente")
    void eliminarBorraCupsCorrectamente() {
        assertDoesNotThrow(() -> service.eliminar(CUPS_TEST));
        assertNull(service.findByCodigo(CUPS_TEST));
    }

    @Test @Order(13)
    @DisplayName("eliminar lanza excepción si no existe")
    void eliminarLanzaExcepcionSiNoExiste() {
        assertThrows(IllegalArgumentException.class,
                () -> service.eliminar("NOEXISTE_JAMAS"));
    }
}