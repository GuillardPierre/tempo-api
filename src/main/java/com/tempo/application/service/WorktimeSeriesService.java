package com.tempo.application.service;

import com.tempo.application.model.worktimeSeries.WorktimeSeries;
import com.tempo.application.repository.CategoryRepository;
import com.tempo.application.repository.UserRepository;
import com.tempo.application.repository.WorkTimeSeriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorktimeSeriesService {

    @Autowired
    private WorkTimeSeriesRepository repository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    public WorktimeSeries createWorktimeSeries(WorktimeSeries request) {

            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new IllegalArgumentException("Start date must be before end date.");
            }

            if (!userRepository.existsById(request.getUser().getId())) {
                throw new IllegalArgumentException("User not found.");
            }

            if (!categoryRepository.existsById(request.getCategory().getId())) {
                throw new IllegalArgumentException("Category not found.");
            }

            return repository.save(request);

    }
}
