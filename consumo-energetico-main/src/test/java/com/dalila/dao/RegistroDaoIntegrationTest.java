package com.dalila.dao;

import com.dalila.db.Db;
import com.dalila.dto.RegistroDTO;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de INTEGRACIÓN para RegistroDao.
 * Usan la BD real pero cada test se ejecuta dentro de una transacción
 * que se revierte al terminar — los datos no quedan guardados.
 *
 * Requisito: la BD consumo_energetico debe estar levantada en localhost:3306
 *
 * Ejecutar: mvn test
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RegistroDaoIntegrationTest {

    private RegistroDao dao;
    private Connection conn;

    // CUPS que sabemos que existe en la BD (cámbialo si en tu BD es diferente)
    private static final String CUPS_EXISTENTE = "ES0031601138661001QF0F";

    @BeforeAll
    void conectar() throws SQLException {
        conn = Db.getConnection();
        dao = new RegistroDao();
    }

    @AfterAll
    void desconectar() throws SQLException {
        if (conn != null && !conn.isClosed()) conn.close();
    }

    @BeforeEach
    void iniciarTransaccion() throws SQLException {
        // Desactivar autocommit → todo lo que hagamos se puede revertir
        conn.setAutoCommit(false);
    }

    @AfterEach
    void revertirTransaccion() throws SQLException {
        // ROLLBACK: deshace los cambios del test, la BD queda como estaba
        conn.rollback();
        conn.setAutoCommit(true);
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("findAll devuelve una lista no vacía")
    void findAllDevuelveListaNoVacia() {
        List<RegistroDTO> lista = dao.findAll();
        assertNotNull(lista);
        assertFalse(lista.isEmpty(), "La BD debería tener registros");
    }

    @Test
    @Order(2)
    @DisplayName("findAll devuelve registros con fecha no nula")
    void findAllDevuelveRegistrosConFecha() {
        List<RegistroDTO> lista = dao.findAll();
        assertTrue(lista.stream().allMatch(r -> r.getFecha() != null),
                "Todos los registros deben tener fecha");
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("findById devuelve null para un ID inexistente")
    void findByIdDevuelveNullParaIdInexistente() {
        RegistroDTO resultado = dao.findById(Integer.MAX_VALUE);
        assertNull(resultado);
    }

    @Test
    @Order(4)
    @DisplayName("findById devuelve el registro correcto para el primer ID")
    void findByIdDevuelveRegistroCorrecto() {
        // Obtenemos el primer registro para tener un ID válido
        List<RegistroDTO> lista = dao.findAll();
        assertFalse(lista.isEmpty());

        int primerIdReal = lista.get(0).getId();
        RegistroDTO resultado = dao.findById(primerIdReal);

        assertNotNull(resultado);
        assertEquals(primerIdReal, resultado.getId());
    }

    // ── insert + findById ─────────────────────────────────────────────────────

    @Test
    @Order(5)
    @DisplayName("insert crea un registro que luego se puede recuperar")
    void insertCreaRegistroRecuperable() throws SQLException {
        // Usamos la conexión con transacción para que el insert sea visible
        // dentro de esta transacción pero se revierta al terminar el test
        RegistroDTO nuevo = new RegistroDTO();
        // Usamos una fecha única basada en el timestamp para evitar duplicados
        String fechaUnica = "2088-" + String.format("%02d", (System.currentTimeMillis() % 12) + 1)
                + "-" + String.format("%02d", (System.currentTimeMillis() % 28) + 1);
        nuevo.setCups(CUPS_EXISTENTE);
        nuevo.setFecha(fechaUnica);
        nuevo.setConsumo(99999.99);

        int id = dao.insert(nuevo);
        assertTrue(id > 0, "Debe devolver un ID válido");

        RegistroDTO recuperado = dao.findById(id);
        assertNotNull(recuperado, "Debe encontrar el registro recién insertado");
        assertEquals(99999.99, recuperado.getConsumo(), 0.001);

        // Limpiar el registro creado
        dao.delete(id);
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("update modifica el consumo de un registro existente")
    void updateModificaConsumo() {
        List<RegistroDTO> lista = dao.findAll();
        assertFalse(lista.isEmpty());

        RegistroDTO original = lista.get(0);
        int id = original.getId();
        double consumoOriginal = original.getConsumo();
        double consumoNuevo = consumoOriginal + 1000.0;

        RegistroDTO modificado = new RegistroDTO();
        modificado.setCups(original.getCups());
        modificado.setFecha(original.getFecha());
        modificado.setConsumo(consumoNuevo);

        dao.update(id, modificado);

        RegistroDTO actualizado = dao.findById(id);
        assertNotNull(actualizado);
        assertEquals(consumoNuevo, actualizado.getConsumo(), 0.001);

        // Revertir el cambio para no dejar datos sucios
        dao.update(id, original);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("delete elimina el registro y ya no se puede recuperar")
    void deleteEliminaRegistro() {
        // Insertamos uno nuevo para eliminarlo (no tocamos datos reales)
        RegistroDTO nuevo = new RegistroDTO();
        nuevo.setCups(CUPS_EXISTENTE);
        nuevo.setFecha("2087-06-15");
        nuevo.setConsumo(1.0);
        int id = dao.insert(nuevo);
        assertTrue(id > 0);

        dao.delete(id);

        RegistroDTO eliminado = dao.findById(id);
        assertNull(eliminado, "El registro eliminado no debe encontrarse");
    }

    // ── findFiltered ──────────────────────────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("findFiltered sin filtros devuelve todos los registros")
    void findFilteredSinFiltrosDevuelveTodos() {
        List<RegistroDTO> todos    = dao.findAll();
        List<RegistroDTO> filtrado = dao.findFiltered(null, null, null, null, null, null, null);
        assertEquals(todos.size(), filtrado.size());
    }

    @Test
    @Order(9)
    @DisplayName("findFiltered por consumoMin excluye registros menores")
    void findFilteredPorConsumoMin() {
        List<RegistroDTO> filtrado = dao.findFiltered(
                null, null, null, null, null, 9000.0, null
        );
        assertTrue(filtrado.stream().allMatch(r -> r.getConsumo() >= 9000.0),
                "Todos los resultados deben tener consumo >= 9000"
        );
    }

    @Test
    @Order(10)
    @DisplayName("findFiltered por fecha devuelve solo los del rango")
    void findFilteredPorFecha() {
        List<RegistroDTO> filtrado = dao.findFiltered(
                null, null, null, "2023", "2023", null, null
        );
        assertTrue(filtrado.stream().allMatch(r -> r.getFecha().startsWith("2023")),
                "Todos los resultados deben ser de 2023"
        );
    }
}