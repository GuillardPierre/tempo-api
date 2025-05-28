package com.tempo.application.repository;

import com.tempo.application.model.worktimeSeries.WorktimeSeries;
import com.tempo.application.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkTimeSeriesRepository extends JpaRepository<WorktimeSeries, Integer> {
    List<WorktimeSeries> findByUser(User user);
    List<WorktimeSeries> findByUserAndActiveTrue(User user);

    @Query("SELECT s FROM WorktimeSeries s WHERE s.user.id = :userId AND s.active = true " +
           "AND (s.endDate IS NULL OR s.endDate >= :from) " +
           "AND s.startDate <= :to")
    List<WorktimeSeries> findActiveByUserAndPeriod(@Param("userId") Integer userId,
                                                  @Param("from") LocalDateTime from,
                                                  @Param("to") LocalDateTime to);
}
