package com.tempo.application.repository;

import com.tempo.application.model.worktimeSeries.WorktimeSeries;
import com.tempo.application.model.user.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkTimeSeriesRepository extends JpaRepository<WorktimeSeries, Integer> {
    List<WorktimeSeries> findByUser(User user);
    List<WorktimeSeries> findByUserAndActiveTrue(User user);
}
