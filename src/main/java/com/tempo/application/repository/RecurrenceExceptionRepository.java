package com.tempo.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tempo.application.model.recurrenceException.RecurrenceException;

public interface RecurrenceExceptionRepository extends JpaRepository<RecurrenceException, Integer> {
}
