package com.tempo.application.repository;

import com.tempo.application.model.worktimeSeries.WorktimeSeries;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkTimeSeriesRepository extends JpaRepository<WorktimeSeries, Integer> {
}
