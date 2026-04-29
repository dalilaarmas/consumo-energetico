package com.dalila.service;

import com.dalila.dto.ConsumoAnualDto;
import com.dalila.dto.RegistroDTO;
import com.dalila.dto.ResumenGlobalDto;
import com.dalila.dto.ResumenGlobalDto;

import java.util.*;
import java.util.stream.Collectors;

public class EstadisticasService {

    public ResumenGlobalDto calcularResumenGlobal(List<RegistroDTO> todos) {
        if (todos == null || todos.isEmpty()) return new ResumenGlobalDto();

        // 1. Récords: Mayor y Menor
        RegistroDTO mayor = todos.stream().max(Comparator.comparing(RegistroDTO::getConsumo)).orElse(null);
        RegistroDTO menor = todos.stream().min(Comparator.comparing(RegistroDTO::getConsumo)).orElse(null);

        // 2. Tops 3
        List<RegistroDTO> top3M = todos.stream()
                .sorted(Comparator.comparing(RegistroDTO::getConsumo).reversed())
                .limit(3).collect(Collectors.toList());

        List<RegistroDTO> top3n = todos.stream()
                .sorted(Comparator.comparing(RegistroDTO::getConsumo))
                .limit(3).collect(Collectors.toList());

        // 3. Cálculos por Año (Agrupación real)
        Map<String, Double> porAnio = todos.stream()
                .collect(Collectors.groupingBy(r -> r.getFecha().substring(0, 4),
                        Collectors.summingDouble(RegistroDTO::getConsumo)));

        Map.Entry<String, Double> maxAnio = porAnio.entrySet().stream()
                .max(Map.Entry.comparingByValue()).orElse(null);

        Map.Entry<String, Double> minAnio = porAnio.entrySet().stream()
                .min(Map.Entry.comparingByValue()).orElse(null);

        // 4. Mes con menor consumo histórico (Agrupamos por "Año-Mes")
        Map<String, Double> porMesAnio = todos.stream()
                .collect(Collectors.groupingBy(r -> r.getFecha().substring(0, 7),
                        Collectors.summingDouble(RegistroDTO::getConsumo)));

        Map.Entry<String, Double> minMes = porMesAnio.entrySet().stream()
                .min(Map.Entry.comparingByValue()).orElse(null);

        return new ResumenGlobalDto(
                mayor, menor, top3M, top3n,
                maxAnio != null ? maxAnio.getKey() : "-", maxAnio != null ? maxAnio.getValue() : 0.0,
                minAnio != null ? minAnio.getKey() : "-", minAnio != null ? minAnio.getValue() : 0.0,
                minMes != null ? minMes.getKey() : "-", minMes != null ? minMes.getValue() : 0.0
        );
    }

    public List<ConsumoAnualDto> calcularResumenAnual(List<RegistroDTO> todos) {
        if (todos == null || todos.isEmpty()) return new ArrayList<>();

        return todos.stream()
                .collect(Collectors.groupingBy(r -> r.getFecha().substring(0, 4)))
                .entrySet().stream()
                .map(entry -> {
                    String anio = entry.getKey();
                    List<RegistroDTO> regs = entry.getValue();

                    double total = regs.stream().mapToDouble(RegistroDTO::getConsumo).sum();

                    Map<String, Double> porMes = regs.stream()
                            .collect(Collectors.groupingBy(r -> r.getFecha().substring(0, 7),
                                    Collectors.summingDouble(RegistroDTO::getConsumo)));

                    Map.Entry<String, Double> maxMes = porMes.entrySet().stream()
                            .max(Map.Entry.comparingByValue()).orElse(null);

                    return new ConsumoAnualDto(
                            Integer.parseInt(anio),
                            total,
                            total / porMes.size(),
                            maxMes != null ? maxMes.getKey() : "-",
                            maxMes != null ? maxMes.getValue() : 0.0
                    );
                })
                .sorted((a, b) -> Integer.compare(b.getAnio(), a.getAnio()))
                .collect(Collectors.toList());
    }
}