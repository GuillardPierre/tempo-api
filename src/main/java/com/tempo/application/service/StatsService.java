package com.tempo.application.service;

import com.tempo.application.model.worktime.DTO.CategoryStatDTO;
import com.tempo.application.model.worktimeSeries.WorktimeSeries;
import com.tempo.application.repository.WorktimeRepository;
import com.tempo.application.repository.WorkTimeSeriesRepository;
import com.tempo.application.utils.RecurrenceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {
    @Autowired
    private WorktimeRepository worktimeRepository;
    @Autowired
    private WorkTimeSeriesRepository workTimeSeriesRepository;

    @Cacheable(value = "categoryStats", key = "#userId + ':' + #from + ':' + #to")
    public List<CategoryStatDTO> getCategoryStats(Integer userId, LocalDateTime from, LocalDateTime to) {
        // 1. Récupérer les worktimes ponctuels
        List<CategoryStatDTO> ponctuels = worktimeRepository.getCategoryStatsByUserAndPeriod(userId, from, to);
        Map<String, Integer> totalDurations = new HashMap<>();
        for (CategoryStatDTO dto : ponctuels) {
            totalDurations.put(dto.getName(), dto.getDuration());
        }

        // 2. Récupérer les séries récurrentes actives
        List<WorktimeSeries> seriesList = workTimeSeriesRepository.findActiveByUserAndPeriod(userId, from, to);
        for (WorktimeSeries series : seriesList) {
            // Générer les occurrences dans la période
            List<LocalDateTime> occurrences = RecurrenceUtils.generateOccurrences(
                series.getRecurrence(),
                series.getStartDate(),
                from,
                to
            );
            // Filtrer les occurrences selon les exceptions
            if (series.getExceptions() != null && !series.getExceptions().isEmpty()) {
                occurrences = occurrences.stream().filter(occ ->
                    series.getExceptions().stream().noneMatch(ex ->
                        !occ.isBefore(ex.getPauseStart()) && !occ.isAfter(ex.getPauseEnd())
                    )
                ).collect(Collectors.toList());
            }
            int totalMinutes = occurrences.size() * series.getDuration().intValue();
            String catName = series.getCategory().getName();
            totalDurations.merge(catName, totalMinutes, Integer::sum);
        }
        // 3. Retourner la liste finale
        return totalDurations.entrySet().stream()
            .map(e -> new CategoryStatDTO(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }
} 