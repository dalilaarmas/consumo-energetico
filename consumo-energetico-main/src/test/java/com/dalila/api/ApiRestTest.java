package com.dalila.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests REST para todos los endpoints de la API.
 * Usan REST Assured para hacer peticiones HTTP reales.
 *
 * REQUISITO: Tomcat debe estar corriendo con la app desplegada.
 *
 * Ejecutar SOLO estos tests:
 *   mvn test -Dgroups=rest
 *
 * Ejecutar todo EXCEPTO estos (mvn test normal):
 *   mvn test
 *
 * Si cambias el puerto o contexto, ajusta BASE_URI y BASE_PATH abajo.
 */
@Tag("rest")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiRestTest {

    private static final String BASE_URI  = "http://localhost:8080";
    private static final String BASE_PATH = "/consumo-energetico/api";

    // ID de registro que se creará en el test de POST y se usará en PUT/DELETE
    private static int idRegistroCreado = -1;

    @BeforeAll
    static void configurar() {
        RestAssured.baseURI  = BASE_URI;
        RestAssured.basePath = BASE_PATH;
    }

    // =========================================================
    // GET /registros
    // =========================================================

    @Test
    @Order(1)
    @DisplayName("GET /registros → 200 y lista no vacía")
    void getRegistrosDevuelveListaNoVacia() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/registros")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", not(empty()))
                .body("[0].id",       notNullValue())
                .body("[0].municipio",notNullValue())
                .body("[0].cups",     notNullValue())
                .body("[0].fecha",    notNullValue())
                .body("[0].consumo",  notNullValue());
    }

    @Test
    @Order(2)
    @DisplayName("GET /registros?municipio=ADEJE → solo registros de ADEJE")
    void getRegistrosFiltradosPorMunicipio() {
        given()
                .accept(ContentType.JSON)
                .queryParam("municipio", "ADEJE")
                .when()
                .get("/registros")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("municipio", everyItem(containsStringIgnoringCase("ADEJE")));
    }

    @Test
    @Order(3)
    @DisplayName("GET /registros?consumoMin=1000 → solo registros con consumo >= 1000")
    void getRegistrosFiltradosPorConsumoMin() {
        given()
                .accept(ContentType.JSON)
                .queryParam("consumoMin", 1000)
                .when()
                .get("/registros")
                .then()
                .statusCode(200)
                .body("consumo", everyItem(greaterThanOrEqualTo(1000f)));
    }

    @Test
    @Order(4)
    @DisplayName("GET /registros?fechaDesde=2023&fechaHasta=2023 → solo registros de 2023")
    void getRegistrosFiltradosPorFecha() {
        given()
                .accept(ContentType.JSON)
                .queryParam("fechaDesde", "2023")
                .queryParam("fechaHasta", "2023")
                .when()
                .get("/registros")
                .then()
                .statusCode(200)
                .body("fecha", everyItem(startsWith("2023")));
    }

    // =========================================================
    // GET /registros/{id}
    // =========================================================

    @Test
    @Order(5)
    @DisplayName("GET /registros/1 → 200 con datos del registro")
    void getRegistroPorIdExistente() {
        // Primero obtenemos un ID real
        int primerID = given()
                .accept(ContentType.JSON)
                .when()
                .get("/registros")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getInt("[0].id");

        given()
                .accept(ContentType.JSON)
                .when()
                .get("/registros/" + primerID)
                .then()
                .statusCode(200)
                .body("id", equalTo(primerID));
    }

    @Test
    @Order(6)
    @DisplayName("GET /registros/999999999 → 404")
    void getRegistroPorIdInexistente() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/registros/999999999")
                .then()
                .statusCode(404);
    }

    // =========================================================
    // POST /registros
    // =========================================================

    @Test
    @Order(7)
    @DisplayName("POST /registros → 201 crea registro correctamente")
    void postRegistroCreaCorrectamente() {
        String body = """
            {
              "cups": "ES0031601106517001TW0F",
              "fecha": "2099-06-15",
              "consumo": 42.42
            }
            """;

        idRegistroCreado = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(body)
                .when()
                .post("/registros")
                .then()
                .statusCode(201)
                .body("cups",    equalTo("ES0031601106517001TW0F"))
                .body("consumo", equalTo(42.42f))
                .extract()
                .jsonPath()
                .getInt("id");

        // Guardar el ID para usarlo en los siguientes tests
        System.out.println("Registro creado con ID: " + idRegistroCreado);
    }

    @Test
    @Order(8)
    @DisplayName("POST /registros sin cups → 400")
    void postRegistroSinCupsDa400() {
        String body = """
            {
              "fecha": "2099-06-15",
              "consumo": 42.42
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/registros")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(9)
    @DisplayName("POST /registros sin fecha → 400")
    void postRegistroSinFechaDa400() {
        String body = """
            {
              "cups": "ES0031601106517001TW0F",
              "consumo": 42.42
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/registros")
                .then()
                .statusCode(400);
    }

    // =========================================================
    // PUT /registros/{id}
    // =========================================================

    @Test
    @Order(10)
    @DisplayName("PUT /registros/{id} → 200 actualiza consumo")
    void putRegistroActualizaConsumo() {
        Assumptions.assumeTrue(idRegistroCreado > 0, "Se necesita un registro creado previamente");

        String body = """
            {
              "cups": "ES0031601106517001TW0F",
              "fecha": "2099-06-15",
              "consumo": 99.99
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(body)
                .when()
                .put("/registros/" + idRegistroCreado)
                .then()
                .statusCode(200)
                .body("consumo", equalTo(99.99f));
    }

    @Test
    @Order(11)
    @DisplayName("PUT /registros/999999999 → 404")
    void putRegistroInexistenteDa404() {
        String body = """
            {
              "cups": "ES0031601106517001TW0F",
              "fecha": "2099-06-15",
              "consumo": 10.0
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put("/registros/999999999")
                .then()
                .statusCode(404);
    }

    // =========================================================
    // DELETE /registros/{id}
    // =========================================================

    @Test
    @Order(12)
    @DisplayName("DELETE /registros/{id} → 204 elimina correctamente")
    void deleteRegistroEliminaCorrectamente() {
        Assumptions.assumeTrue(idRegistroCreado > 0, "Se necesita un registro creado previamente");

        given()
                .when()
                .delete("/registros/" + idRegistroCreado)
                .then()
                .statusCode(204);

        // Verificar que ya no existe
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/registros/" + idRegistroCreado)
                .then()
                .statusCode(404);
    }

    @Test
    @Order(13)
    @DisplayName("DELETE /registros/999999999 → 404")
    void deleteRegistroInexistenteDa404() {
        given()
                .when()
                .delete("/registros/999999999")
                .then()
                .statusCode(404);
    }

    // =========================================================
    // GET /registros/resumen
    // =========================================================

    @Test
    @Order(14)
    @DisplayName("GET /registros/resumen → 200 con estructura correcta")
    void getResumenGlobalTieneEstructuraCorrecta() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/registros/resumen")
                .then()
                .statusCode(200)
                .body("diaMayorConsumo",  notNullValue())
                .body("anioMasConsumo",   notNullValue())
                .body("top3DiasMayor",    notNullValue())
                .body("top3DiasMayor.size()", greaterThan(0));
    }

    // =========================================================
    // GET /registros/resumen/anual
    // =========================================================

    @Test
    @Order(15)
    @DisplayName("GET /registros/resumen/anual → lista con años")
    void getResumenAnualDevuelveListaConAnios() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/registros/resumen/anual")
                .then()
                .statusCode(200)
                .body("$",    not(empty()))
                .body("[0].anio",         notNullValue())
                .body("[0].consumoTotal",  notNullValue());
    }

    // =========================================================
    // GET /registros/anio/{anio}/registros
    // =========================================================

    @Test
    @Order(16)
    @DisplayName("GET /registros/anio/2023/registros → solo registros de 2023")
    void getRegistrosPorAnioDevuelveSoloDe2023() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/registros/anio/2023/registros")
                .then()
                .statusCode(200)
                .body("fecha", everyItem(startsWith("2023")));
    }

    // =========================================================
    // GET /registros/anio/{anio}/analisis
    // =========================================================

    @Test
    @Order(17)
    @DisplayName("GET /registros/anio/2023/analisis → estructura de análisis completa")
    void getAnalisisPorAnioTieneEstructuraCorrecta() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/registros/anio/2023/analisis")
                .then()
                .statusCode(200)
                .body("anio",                equalTo(2023))
                .body("consumoTotalAnual",   notNullValue())
                .body("promedioMensualAnual", notNullValue())
                .body("detallePorMes",       notNullValue());
    }

    @Test
    @Order(18)
    @DisplayName("GET /registros/anio/1800/analisis → 404 año sin datos")
    void getAnalisisAnioSinDatosDa404() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/registros/anio/1800/analisis")
                .then()
                .statusCode(404);
    }

    // =========================================================
    // GET /municipios
    // =========================================================

    @Test
    @Order(19)
    @DisplayName("GET /municipios → lista con id y nombre")
    void getMunicipiosDevuelveListaConIdYNombre() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/municipios")
                .then()
                .statusCode(200)
                .body("$",       not(empty()))
                .body("[0].id",  notNullValue())
                .body("[0].nombre", notNullValue());
    }

    @Test
    @Order(20)
    @DisplayName("GET /municipios/1 → municipio ADEJE")
    void getMunicipioPorIdDevuelveAdeje() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/municipios/1")
                .then()
                .statusCode(200)
                .body("id",     equalTo(1))
                .body("nombre", notNullValue());
    }

    @Test
    @Order(21)
    @DisplayName("GET /municipios/999999 → 404")
    void getMunicipioInexistenteDa404() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/municipios/999999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(22)
    @DisplayName("GET /municipios/1/cups → lista de CUPS del municipio 1")
    void getCupsPorMunicipioDevuelveLista() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/municipios/1/cups")
                .then()
                .statusCode(200)
                .body("$",            not(empty()))
                .body("[0].codigo",   notNullValue())
                .body("[0].municipio", equalTo("ADEJE"));
    }

    // =========================================================
    // GET /cups/{codigo}
    // =========================================================

    @Test
    @Order(23)
    @DisplayName("GET /cups/{codigo} → devuelve CUPS con municipio")
    void getCupsPorCodigoDevuelveMunicipio() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/cups/ES0031601106517001TW0F")
                .then()
                .statusCode(200)
                .body("codigo",    equalTo("ES0031601106517001TW0F"))
                .body("municipio", notNullValue())
                .body("direccion", notNullValue());
    }

    @Test
    @Order(24)
    @DisplayName("GET /cups/CODIGOINEXISTENTE → 404")
    void getCupsInexistenteDa404() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/cups/CODIGOINEXISTENTE99999")
                .then()
                .statusCode(404);
    }
}