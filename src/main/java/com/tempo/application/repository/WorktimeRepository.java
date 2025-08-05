package com.tempo.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tempo.application.model.user.User;
import com.tempo.application.model.worktime.Worktime;
import com.tempo.application.model.category.Category;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorktimeRepository extends JpaRepository<Worktime, Integer> {
    Optional<Worktime> findById(int id);

    // Retourne tous les worktimes liés à un id utilisateur
    List<Worktime> findByUserId(Integer userId);

    // Retourne tous les worktimes pour une plage de dates
    List<Worktime> findByStartHourBetween(LocalDateTime start, LocalDateTime end);
    
    // Retourne tous les worktimes pour une plage de dates et un utilisateur spécifique
    List<Worktime> findByStartHourBetweenAndUser(LocalDateTime start, LocalDateTime end, User user);

    // Récupère tous les worktimes en cours (sans endTime) pour un utilisateur
    List<Worktime> findByUserAndEndHourIsNull(User user);

    void deleteByCategory(Category category);

    @Query(
      value = "SELECT c.name as name, " +
              "CAST(SUM(EXTRACT(EPOCH FROM (w.end_hour - w.start_hour))/60) AS INTEGER) as duration " +
              "FROM worktime w " +
              "JOIN category c ON w.category_id = c.id " +
              "WHERE w.user_id = :userId " +
              "AND w.start_hour >= :from " +
              "AND w.end_hour <= :to " +
              "AND w.end_hour IS NOT NULL " +
              "AND NOT EXISTS (" +
              "    SELECT 1 FROM recurrence_exception_series res " +
              "    JOIN recurrence_exception e ON res.exception_id = e.id " +
              "    JOIN worktime_series s ON res.series_id = s.id " +
              "    WHERE s.ignore_exceptions = false " +
              "    AND w.start_hour >= e.pause_start " +
              "    AND w.end_hour <= e.pause_end" +
              ") " +
              "GROUP BY c.name",
      nativeQuery = true)
    List<Object[]> getCategoryStatsByUserAndPeriodExcludingExceptions(@Param("userId") Integer userId,
                                                          @Param("from") LocalDateTime from,
                                                          @Param("to") LocalDateTime to);

    @Query(
      value = "SELECT c.name as name, " +
              "CAST(SUM(EXTRACT(EPOCH FROM (w.end_hour - w.start_hour))/60) AS INTEGER) as duration " +
              "FROM worktime w " +
              "JOIN category c ON w.category_id = c.id " +
              "WHERE w.user_id = :userId " +
              "AND w.start_hour >= :from " +
              "AND w.end_hour <= :to " +
              "AND w.end_hour IS NOT NULL " +
              "GROUP BY c.name",
      nativeQuery = true)
    List<Object[]> getCategoryStatsByUserAndPeriod(@Param("userId") Integer userId,
                                                          @Param("from") LocalDateTime from,
                                                          @Param("to") LocalDateTime to);

    @Query(
      value = "SELECT DATE(w.start_hour) as date, " +
              "CAST(SUM(EXTRACT(EPOCH FROM (w.end_hour - w.start_hour))/60) AS BIGINT) as duration " +
              "FROM worktime w " +
              "WHERE w.user_id = :userId " +
              "AND w.start_hour >= :from " +
              "AND w.start_hour <= :to " +
              "AND w.end_hour IS NOT NULL " +
              "GROUP BY DATE(w.start_hour)",
      nativeQuery = true)
    List<Object[]> getTotalWorktimeByUserAndPeriodGroupByDay(
        @Param("userId") Integer userId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    @Query(
      value = "SELECT TO_CHAR(w.start_hour, 'YYYY-MM') as period, " +
              "CAST(SUM(EXTRACT(EPOCH FROM (w.end_hour - w.start_hour))/60) AS BIGINT) as duration " +
              "FROM worktime w " +
              "WHERE w.user_id = :userId " +
              "AND w.start_hour >= :from " +
              "AND w.start_hour <= :to " +
              "AND w.end_hour IS NOT NULL " +
              "GROUP BY TO_CHAR(w.start_hour, 'YYYY-MM')",
      nativeQuery = true)
    List<Object[]> getTotalWorktimeByUserAndPeriodGroupByMonth(
        @Param("userId") Integer userId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    @Query(
      value = "SELECT EXTRACT(YEAR FROM w.start_hour) as y, EXTRACT(WEEK FROM w.start_hour) as w, " +
              "CAST(SUM(EXTRACT(EPOCH FROM (w.end_hour - w.start_hour))/60) AS BIGINT) as duration " +
              "FROM worktime w " +
              "WHERE w.user_id = :userId " +
              "AND w.start_hour >= :from " +
              "AND w.start_hour <= :to " +
              "AND w.end_hour IS NOT NULL " +
              "GROUP BY EXTRACT(YEAR FROM w.start_hour), EXTRACT(WEEK FROM w.start_hour)",
      nativeQuery = true)
    List<Object[]> getTotalWorktimeByUserAndPeriodGroupByWeek(
        @Param("userId") Integer userId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );
                                                          
}
