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
            totalDurations.merge(dto.getName(), dto.getDuration(), Integer::sum);
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

    /**
     * Retourne le temps total (toutes catégories confondues) par jour (si période <= 31 jours) ou par mois (sinon).
     * Format de retour : { labels: [...], data: [...] }
     */
    public Map<String, Object> getTotalWorkTime(Integer userId, LocalDateTime from, LocalDateTime to, String type) {
        Map<String, Integer> totalByPeriod = new java.util.TreeMap<>();
        List<Object[]> rows;
        if ("month".equalsIgnoreCase(type)) {
            rows = worktimeRepository.getTotalWorktimeByUserAndPeriodGroupByWeek(userId, from, to);
        } else if ("year".equalsIgnoreCase(type)) {
            rows = worktimeRepository.getTotalWorktimeByUserAndPeriodGroupByMonth(userId, from, to);
        } else if ("week".equalsIgnoreCase(type)) {
            rows = worktimeRepository.getTotalWorktimeByUserAndPeriodGroupByDay(userId, from, to);
        } else {
            throw new IllegalArgumentException("Type de groupement non supporté : " + type);
        }
        for (Object[] row : rows) {
            String period;
            if ("month".equalsIgnoreCase(type)) {
                period = row[0] + "-S" + String.format("%02d", ((Number) row[1]).intValue());
                int duration = ((Number) row[2]).intValue();
                totalByPeriod.put(period, duration);
            } else if ("year".equalsIgnoreCase(type)) {
                period = row[0].toString();
                int duration = ((Number) row[1]).intValue();
                totalByPeriod.put(period, duration);
            } else {
                period = ((java.sql.Date) row[0]).toLocalDate().toString();
                int duration = ((Number) row[1]).intValue();
                totalByPeriod.put(period, duration);
            }
        }

        // Séries récurrentes
        List<com.tempo.application.model.worktimeSeries.WorktimeSeries> seriesList = workTimeSeriesRepository.findActiveByUserAndPeriod(userId, from, to);
        for (com.tempo.application.model.worktimeSeries.WorktimeSeries series : seriesList) {
            java.time.LocalDateTime recurrenceEnd = (series.getEndDate() != null && series.getEndDate().isBefore(to)) ? series.getEndDate() : to;
            List<java.time.LocalDateTime> occurrences = com.tempo.application.utils.RecurrenceUtils.generateOccurrences(
                series.getRecurrence(),
                series.getStartDate(),
                from,
                recurrenceEnd
            );
            for (java.time.LocalDateTime occ : occurrences) {
                String period;
                if ("month".equalsIgnoreCase(type)) {
                    java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.ISO;
                    int weekNumber = occ.get(weekFields.weekOfWeekBasedYear());
                    int year = occ.getYear();
                    period = year + "-S" + String.format("%02d", weekNumber);
                } else if ("year".equalsIgnoreCase(type)) {
                    period = String.format("%d-%02d", occ.getYear(), occ.getMonthValue());
                } else {
                    period = occ.toLocalDate().toString();
                }
                int duration = series.getDuration().intValue();
                totalByPeriod.merge(period, duration, Integer::sum);
            }
        }

        List<String> periods = new java.util.ArrayList<>();
        List<String> labels = new java.util.ArrayList<>();
        List<Integer> data = new java.util.ArrayList<>();
        java.time.format.TextStyle style = java.time.format.TextStyle.FULL;
        java.util.Locale locale = java.util.Locale.FRENCH;

        if ("week".equalsIgnoreCase(type)) {
            // Générer tous les jours de la semaine de 'from'
            java.time.LocalDate start = from.toLocalDate();
            java.time.DayOfWeek firstDay = java.time.DayOfWeek.MONDAY;
            java.time.LocalDate weekStart = start.with(java.time.temporal.TemporalAdjusters.previousOrSame(firstDay));
            for (int i = 0; i < 7; i++) {
                java.time.LocalDate day = weekStart.plusDays(i);
                String period = day.toString();
                periods.add(period);
                labels.add(day.getDayOfWeek().getDisplayName(style, locale));
                data.add(totalByPeriod.getOrDefault(period, 0));
            }
        } else if ("month".equalsIgnoreCase(type)) {
            // Générer toutes les semaines entre from et to
            java.time.LocalDate start = from.toLocalDate().with(java.time.DayOfWeek.MONDAY);
            java.time.LocalDate end = to.toLocalDate();
            java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.ISO;
            java.util.Set<String> seen = new java.util.HashSet<>();
            java.time.LocalDate d = start;
            while (!d.isAfter(end)) {
                int weekNumber = d.get(weekFields.weekOfWeekBasedYear());
                int year = d.getYear();
                String period = year + "-S" + String.format("%02d", weekNumber);
                if (!seen.contains(period)) {
                    periods.add(period);
                    labels.add("Semaine " + weekNumber);
                    data.add(totalByPeriod.getOrDefault(period, 0));
                    seen.add(period);
                }
                d = d.plusWeeks(1);
            }
        } else if ("year".equalsIgnoreCase(type)) {
            // Générer tous les mois entre from et to
            java.time.LocalDate start = from.toLocalDate().withDayOfMonth(1);
            java.time.LocalDate end = to.toLocalDate().withDayOfMonth(1);
            java.time.LocalDate d = start;
            while (!d.isAfter(end)) {
                String period = String.format("%d-%02d", d.getYear(), d.getMonthValue());
                periods.add(period);
                java.time.Month month = d.getMonth();
                labels.add(month.getDisplayName(style, locale));
                data.add(totalByPeriod.getOrDefault(period, 0));
                d = d.plusMonths(1);
            }
        }

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("labels", labels);
        result.put("data", data);
        return result;
    }
} 