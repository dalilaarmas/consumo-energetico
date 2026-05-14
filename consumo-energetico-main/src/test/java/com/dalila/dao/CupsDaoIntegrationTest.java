package com.dalila.dao;

import com.dalila.db.Db;
import com.dalila.dto.CupsDto;
import com.dalila.entity.Cups;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de INTEGRACIÓN para CupsDao.
 * La BD real se usa pero los cambios se revierten con ROLLBACK.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CupsDaoIntegrationTest {

    private CupsDao dao;
    private Connection conn;

    // ID de municipio que existe en tu BD — ajústalo si es necesario
    // Para saber el ID: SELECT id, nombre FROM municipio LIMIT 5;
    private static final int MUNICIPIO_ID_EXISTENTE = 1;
    private static final int DISTRIBUIDOR_ID_EXISTENTE = 1;

    // Código de CUPS que sabemos que existe
    private static final String CUPS_EXISTENTE = "ES0031601138661001QF0F";

    // Código de CUPS inventado para tests de creación
    private static final String CUPS_TEST = "ESTEST99999999999999TEST";

    @BeforeAll
    void conectar() throws SQLException {
        conn = Db.getConnection();
        dao = new CupsDao();
    }

    @AfterAll
    void desconectar() throws SQLException {
        if (conn != null && !conn.isClosed()) conn.close();
    }

    // ── findByCodigo ──────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("findByCodigo devuelve el CUPS con municipio incluido")
    void findByCodigoDevuelveDtoConMunicipio() throws SQLException {
        CupsDto resultado = dao.findByCodigo(CUPS_EXISTENTE);

        assertNotNull(resultado, "El CUPS debe existir en la BD");
        assertEquals(CUPS_EXISTENTE, resultado.getCodigo());
        assertNotNull(resultado.getMunicipio(), "El nombre del municipio no debe ser null");
        assertFalse(resultado.getMunicipio().isBlank(), "El municipio no debe estar vacío");
    }

    @Test
    @Order(2)
    @DisplayName("findByCodigo devuelve null para un código inexistente")
    void findByCodigoDevuelveNullSiNoExiste() throws SQLException {
        CupsDto resultado = dao.findByCodigo("CODIGO_QUE_NO_EXISTE_JAMAS");
        assertNull(resultado);
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("findAll sin municipio devuelve lista no vacía")
    void findAllSinFiltroDevuelveListaNoVacia() throws SQLException {
        List<CupsDto> lista = dao.findAll(null, 10);
        assertNotNull(lista);
        assertFalse(lista.isEmpty());
        assertTrue(lista.size() <= 10, "No debe superar el límite");
    }

    @Test
    @Order(4)
    @DisplayName("findAll con municipio filtra correctamente")
    void findAllConMunicipioFiltrado() throws SQLException {
        // Primero obtenemos un municipio real
        CupsDto cups = dao.findByCodigo(CUPS_EXISTENTE);
        assertNotNull(cups);
        String municipio = cups.getMunicipio();

        List<CupsDto> filtrado = dao.findAll(municipio, 100);

        assertFalse(filtrado.isEmpty());
        assertTrue(filtrado.stream().allMatch(c ->
                c.getMunicipio() != null &&
                        c.getMunicipio().toLowerCase().contains(municipio.toLowerCase())
        ), "Todos deben pertenecer al municipio filtrado");
    }

    @Test
    @Order(5)
    @DisplayName("findAll respeta el límite de resultados")
    void findAllResjetaLimite() throws SQLException {
        List<CupsDto> lista = dao.findAll(null, 5);
        assertTrue(lista.size() <= 5);
    }

    // ── findByMunicipioId ─────────────────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("findByMunicipioId devuelve CUPS del municipio")
    void findByMunicipioIdDevuelveCups() throws SQLException {
        List<CupsDto> lista = dao.findByMunicipioId(MUNICIPIO_ID_EXISTENTE);
        assertNotNull(lista);
        // Todos deben tener el mismo municipioId
        assertTrue(lista.stream().allMatch(c -> c.getMunicipioId() == MUNICIPIO_ID_EXISTENTE),
                "Todos deben pertenecer al municipio " + MUNICIPIO_ID_EXISTENTE
        );
    }

    // ── create + findByCodigo + delete ────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("create inserta un CUPS y delete lo elimina correctamente")
    void createYDeleteFuncionan() throws SQLException {
        // Asegurarnos de que no existe ya
        CupsDto previo = dao.findByCodigo(CUPS_TEST);
        if (previo != null) dao.delete(CUPS_TEST); // limpiar si quedó de un test anterior

        // Crear
        Cups nuevo = new Cups(CUPS_TEST, "Dirección de Test", 38099,
                MUNICIPIO_ID_EXISTENTE, DISTRIBUIDOR_ID_EXISTENTE);
        dao.create(nuevo);

        // Verificar que existe
        CupsDto creado = dao.findByCodigo(CUPS_TEST);
        assertNotNull(creado, "El CUPS recién creado debe existir");
        assertEquals(CUPS_TEST, creado.getCodigo());
        assertEquals("Dirección de Test", creado.getDireccion());

        // Eliminar
        dao.delete(CUPS_TEST);

        // Verificar que ya no existe
        CupsDto eliminado = dao.findByCodigo(CUPS_TEST);
        assertNull(eliminado, "El CUPS eliminado no debe encontrarse");
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("update modifica la dirección de un CUPS existente")
    void updateModificaDireccion() throws SQLException {
        // Crear un CUPS temporal
        Cups temporal = new Cups(CUPS_TEST, "Dirección Original", 38001,
                MUNICIPIO_ID_EXISTENTE, DISTRIBUIDOR_ID_EXISTENTE);
        dao.create(temporal);

        // Modificarlo
        Cups modificado = new Cups(CUPS_TEST, "Dirección Modificada", 38002,
                MUNICIPIO_ID_EXISTENTE, DISTRIBUIDOR_ID_EXISTENTE);
        dao.update(modificado);

        // Verificar
        CupsDto resultado = dao.findByCodigo(CUPS_TEST);
        assertNotNull(resultado);
        assertEquals("Dirección Modificada", resultado.getDireccion());
        assertEquals(38002, resultado.getCodigoPostal());

        // Limpiar
        dao.delete(CUPS_TEST);
    }
}