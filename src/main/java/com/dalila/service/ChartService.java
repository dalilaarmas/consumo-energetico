package com.dalila.service;

import com.dalila.dto.RegistroDTO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ChartService {

    public BufferedImage generarGraficaConsumo(List<RegistroDTO> registros) {
        Map<String, Double> agrupado = agruparPorFecha(registros);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, Double> entry : agrupado.entrySet()) {
            dataset.addValue(entry.getValue(), "Consumo energético", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Evolución del consumo energético",
                "Fecha",
                "Consumo (kWh)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false
        );

        return chart.createBufferedImage(1000, 450);
    }

    private Map<String, Double> agruparPorFecha(List<RegistroDTO> registros) {
        Map<String, Double> agrupado = new TreeMap<>();

        for (RegistroDTO r : registros) {
            if (r.getFecha() == null || r.getConsumo() == null) continue;

            String clave = r.getFecha();
            agrupado.put(clave, agrupado.getOrDefault(clave, 0.0) + r.getConsumo());
        }

        return agrupado;
    }
}