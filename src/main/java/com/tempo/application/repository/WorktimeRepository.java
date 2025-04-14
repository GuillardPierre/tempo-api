package com.tempo.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tempo.application.model.worktime.Worktime;

public interface WorktimeRepository extends JpaRepository<Worktime, Integer> {
}
