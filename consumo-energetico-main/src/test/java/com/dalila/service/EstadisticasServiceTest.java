package com.dalila.service;

import com.dalila.dto.ConsumoAnualDto;
import com.dalila.dto.DetalleEstadisticoAnualDTO;
import com.dalila.dto.RegistroDTO;
import com.dalila.dto.ResumenGlobalDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EstadisticasServiceTest {

    private EstadisticasService service;
    private List<RegistroDTO> registros;

    @BeforeEach
    void setUp() {
        service = new EstadisticasService();
        registros = List.of(
                new RegistroDTO(1, "ADEJE",    "ES001", "Dir A", "2022-01-15", 100.0),
                new RegistroDTO(2, "ADEJE",    "ES001", "Dir A", "2022-06-20", 200.0),
                new RegistroDTO(3, "LAGUNA",   "ES002", "Dir B", "2022-09-25", 5009.0),
                new RegistroDTO(4, "LAGUNA",   "ES002", "Dir B", "2023-03-10", 300.0),
                new RegistroDTO(5, "LAGUNA",   "ES002", "Dir B", "2023-07-15", 400.0),
                new RegistroDTO(6, "ADEJE",    "ES001", "Dir A", "2023-11-20", 4919.0),
                new RegistroDTO(7, "TACORONTE","ES003", "Dir C", "2024-05-01", 0.01)
        );
    }

    @Nested
    @DisplayName("calcularResumenGlobal")
    class ResumenGlobalTests {

        @Test
        @DisplayName("Con lista vacía devuelve objeto no nulo")
        void conListaVaciaDevuelveObjetoNoNulo() {
            ResumenGlobalDto resultado = service.calcularResumenGlobal(List.of());
            assertNotNull(resultado);
        }

        @Test
        @DisplayName("Con lista nula devuelve objeto no nulo")
        void conListaNulaDevuelveObjetoNoNulo() {
            ResumenGlobalDto resultado = service.calcularResumenGlobal(null);
            assertNotNull(resultado);
        }

        @Test
        @DisplayName("Identifica correctamente el día de mayor consumo")
        void identificaDiaDeMayorConsumo() {
            ResumenGlobalDto resultado = service.calcularResumenGlobal(registros);
            assertNotNull(resultado.getDiaMayorConsumo());
            assertEquals(5009.0, resultado.getDiaMayorConsumo().getConsumo());
            assertEquals("2022-09-25", resultado.getDiaMayorConsumo().getFecha());
        }

        @Test
        @DisplayName("Identifica correctamente el año con más consumo")
        void identificaAnioConMasConsumo() {
            ResumenGlobalDto resultado = service.calcularResumenGlobal(registros);
            // 2022: 5309 | 2023: 5619 | 2024: 0.01
            assertEquals("2023", resultado.getAnioMasConsumo());
        }

        @Test
        @DisplayName("El top 3 mayor tiene como máximo 3 elementos")
        void top3TieneMaximo3Elementos() {
            ResumenGlobalDto resultado = service.calcularResumenGlobal(registros);
            assertNotNull(resultado.getTop3DiasMayor());
            assertTrue(resultado.getTop3DiasMayor().size() <= 3);
        }

        @Test
        @DisplayName("El top 3 mayor está ordenado de mayor a menor")
        void top3MayorOrdenadoCorrectamente() {
            ResumenGlobalDto resultado = service.calcularResumenGlobal(registros);
            List<RegistroDTO> top = resultado.getTop3DiasMayor();
            for (int i = 0; i < top.size() - 1; i++) {
                assertTrue(top.get(i).getConsumo() >= top.get(i + 1).getConsumo());
            }
        }
    }

    @Nested
    @DisplayName("calcularResumenAnual")
    class ResumenAnualTests {

        @Test
        @DisplayName("Con lista vacía devuelve lista vacía")
        void conListaVaciaDevuelveListaVacia() {
            List<ConsumoAnualDto> resultado = service.calcularResumenAnual(List.of());
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Agrupa correctamente por año — debe haber 3 años")
        void agrupaPorAnio() {
            List<ConsumoAnualDto> resultado = service.calcularResumenAnual(registros);
            assertEquals(3, resultado.size());
        }

        @Test
        @DisplayName("Suma correctamente el total anual de 2022")
        void sumaTotalAnual2022() {
            List<ConsumoAnualDto> resultado = service.calcularResumenAnual(registros);
            ConsumoAnualDto anio2022 = resultado.stream()
                    .filter(a -> a.getAnio() == 2022)
                    .findFirst()
                    .orElse(null);

            assertNotNull(anio2022, "Debe existir entrada para 2022");
            assertEquals(5309.0, anio2022.getConsumoTotal(), 0.001);
        }

        @Test
        @DisplayName("La lista está ordenada de más reciente a más antiguo")
        void ordenadoMasRecientePrimero() {
            List<ConsumoAnualDto> resultado = service.calcularResumenAnual(registros);
            for (int i = 0; i < resultado.size() - 1; i++) {
                assertTrue(resultado.get(i).getAnio() >= resultado.get(i + 1).getAnio());
            }
        }
    }

    @Nested
    @DisplayName("obtenerAnalisisCompleto")
    class AnalisisCompletoTests {

        @Test
        @DisplayName("Para un año sin datos devuelve null")
        void anioSinDatosDevuelveNull() {
            DetalleEstadisticoAnualDTO resultado = service.obtenerAnalisisCompleto(1999, registros);
            assertNull(resultado);
        }

        @Test
        @DisplayName("Calcula correctamente el total anual de 2022")
        void calculaTotalAnual2022() {
            DetalleEstadisticoAnualDTO resultado = service.obtenerAnalisisCompleto(2022, registros);
            assertNotNull(resultado);
            assertEquals(5309.0, resultado.getConsumoTotalAnual(), 0.001);
        }

        @Test
        @DisplayName("El desglose por mes no está vacío para 2022")
        void desglosePorMesNoVacio() {
            DetalleEstadisticoAnualDTO resultado = service.obtenerAnalisisCompleto(2022, registros);
            assertNotNull(resultado.getDetallePorMes());
            assertFalse(resultado.getDetallePorMes().isEmpty());
        }

        @Test
        @DisplayName("El promedio mensual de 2022 es correcto")
        void promedioMensual2022() {
            DetalleEstadisticoAnualDTO resultado = service.obtenerAnalisisCompleto(2022, registros);
            // 3 meses distintos en 2022: total 5309 / 3 = 1769.67
            assertEquals(5309.0 / 3, resultado.getPromedioMensualAnual(), 0.01);
        }
    }
}