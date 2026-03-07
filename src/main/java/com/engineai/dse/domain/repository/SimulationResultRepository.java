package com.engineai.dse.domain.repository;

import com.engineai.dse.domain.model.SimulationResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SimulationResultRepository extends JpaRepository<SimulationResult, Long> {

    List<SimulationResult> findByUserIdOrderByCreatedAtDesc(Long userId);
}