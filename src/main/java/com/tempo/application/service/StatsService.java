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
import com.tempo.application.model.recurrenceException.RecurrenceException;

@Service
public class StatsService {
    @Autowired
    private WorktimeRepository worktimeRepository;
    @Autowired
    private WorkTimeSeriesRepository workTimeSeriesRepository;

    private LocalDateTime calculateRecurrenceEnd(WorktimeSeries series, LocalDateTime requestedEndDate) {
        return (series.getEndDate() != null && series.getEndDate().isBefore(requestedEndDate)) 
            ? series.getEndDate() 
            : requestedEndDate;
    }

    /**
     * Vérifie si une occurrence tombe dans la période d'une exception.
     * Une occurrence est considérée comme "dans l'exception" si elle tombe
     * le même jour que la période d'exception (même si elle est après la fin de la pause).
     * 
     * @param occurrence La date/heure de l'occurrence à vérifier
     * @param exception L'exception à vérifier
     * @return true si l'occurrence est dans la période d'exception, false sinon
     */
    private boolean isInExceptionPeriod(LocalDateTime occurrence, RecurrenceException exception) {
        // Vérifier si l'occurrence tombe le même jour que la période d'exception
        return !occurrence.toLocalDate().isBefore(exception.getPauseStart().toLocalDate()) &&
               !occurrence.toLocalDate().isAfter(exception.getPauseEnd().toLocalDate());
    }

    // @Cacheable(value = "categoryStats", key = "#userId + ':' + #from + ':' + #to") // Temporairement désactivé pour debug
    public List<CategoryStatDTO> getCategoryStats(Integer userId, LocalDateTime from, LocalDateTime to) {
        System.out.println("DEBUG: getCategoryStats called with userId=" + userId + ", from=" + from + ", to=" + to);
        
        // 1. Récupérer tous les worktimes ponctuels (pas besoin de filtrer)
        List<Object[]> ponctuelsRaw = worktimeRepository.getCategoryStatsByUserAndPeriod(userId, from, to);
        System.out.println("DEBUG: Found " + ponctuelsRaw.size() + " worktime entries from SQL query");
        for (Object[] row : ponctuelsRaw) {
            System.out.println("DEBUG: Category=" + row[0] + ", Duration=" + row[1]);
        }
        
        Map<String, Integer> totalDurations = new HashMap<>();
        for (Object[] row : ponctuelsRaw) {
            String categoryName = (String) row[0];
            Integer duration = (Integer) row[1];
            totalDurations.merge(categoryName, duration, Integer::sum);
        }

        // 2. Récupérer les séries récurrentes
        List<WorktimeSeries> seriesList = workTimeSeriesRepository.findByUserAndPeriod(userId, from, to);
        for (WorktimeSeries series : seriesList) {
            LocalDateTime recurrenceEnd = calculateRecurrenceEnd(series, to);

            String catName = series.getCategory() != null ? series.getCategory().getName() : "<no-cat>";
            Boolean ignoreExceptions = series.getIgnoreExceptions();

            // Si ignoreExceptions est true, on traite la série normalement
            // Si ignoreExceptions est null, on considère qu'il est false
            if (Boolean.TRUE.equals(ignoreExceptions)) {
                List<LocalDateTime> occurrences = RecurrenceUtils.generateOccurrences(
                    series.getRecurrence(),
                    series.getStartDate(),
                    from,
                    recurrenceEnd,
                    series.getStartHour() != null ? series.getStartHour() : series.getStartDate()
                );
                int totalMinutes = occurrences.size() * series.getDuration().intValue();
                totalDurations.merge(catName, totalMinutes, Integer::sum);
                continue;
            }

            // Sinon (ignoreExceptions est false ou null), on filtre les occurrences selon les exceptions
            List<LocalDateTime> occurrences = RecurrenceUtils.generateOccurrences(
                series.getRecurrence(),
                series.getStartDate(),
                from,
                recurrenceEnd,
                series.getStartHour() != null ? series.getStartHour() : series.getStartDate()
            );
            
            if (series.getExceptions() != null && !series.getExceptions().isEmpty()) {
                occurrences = occurrences.stream()
                    .filter(occ -> series.getExceptions().stream()
                        .noneMatch(ex -> isInExceptionPeriod(occ, ex))
                    )
                    .collect(Collectors.toList());
            }
            
            int totalMinutes = occurrences.size() * series.getDuration().intValue();
            totalDurations.merge(catName, totalMinutes, Integer::sum);
        }

        // 3. Retourner la liste finale
        List<CategoryStatDTO> result = totalDurations.entrySet().stream()
            .map(e -> new CategoryStatDTO(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
        return result;
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
        List<WorktimeSeries> seriesList = workTimeSeriesRepository.findByUserAndPeriod(userId, from, to);
        for (WorktimeSeries series : seriesList) {
            LocalDateTime recurrenceEnd = calculateRecurrenceEnd(series, to);

            // Si ignoreExceptions est true, on traite la série normalement
            // Si ignoreExceptions est null, on considère qu'il est false
            Boolean ignoreExceptions = series.getIgnoreExceptions();
            if (Boolean.TRUE.equals(ignoreExceptions)) {
                List<LocalDateTime> occurrences = RecurrenceUtils.generateOccurrences(
                    series.getRecurrence(),
                    series.getStartDate(),
                    from,
                    recurrenceEnd,
                    series.getStartHour() != null ? series.getStartHour() : series.getStartDate()
                );
                for (LocalDateTime occ : occurrences) {
                    String period = formatPeriod(occ, type);
                    int duration = series.getDuration().intValue();
                    totalByPeriod.merge(period, duration, Integer::sum);
                }
                continue;
            }

            // Sinon (ignoreExceptions est false ou null), on filtre les occurrences selon les exceptions
            List<LocalDateTime> occurrences = RecurrenceUtils.generateOccurrences(
                series.getRecurrence(),
                series.getStartDate(),
                from,
                recurrenceEnd,
                series.getStartHour() != null ? series.getStartHour() : series.getStartDate()
            );

            if (series.getExceptions() != null && !series.getExceptions().isEmpty()) {
                occurrences = occurrences.stream()
                    .filter(occ -> series.getExceptions().stream()
                        .noneMatch(ex -> isInExceptionPeriod(occ, ex))
                    )
                    .collect(Collectors.toList());
            }

            for (LocalDateTime occ : occurrences) {
                String period = formatPeriod(occ, type);
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
                    labels.add(String.format("%02d/%02d", d.getDayOfMonth(), d.getMonthValue()));
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

    private String formatPeriod(LocalDateTime date, String type) {
        if ("month".equalsIgnoreCase(type)) {
            java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.ISO;
            int weekNumber = date.get(weekFields.weekOfWeekBasedYear());
            int year = date.getYear();
            return year + "-S" + String.format("%02d", weekNumber);
        } else if ("year".equalsIgnoreCase(type)) {
            return String.format("%d-%02d", date.getYear(), date.getMonthValue());
        } else {
            return date.toLocalDate().toString();
        }
    }
} 