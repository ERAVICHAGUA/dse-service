package com.engineai.dse.domain.repository;

import com.engineai.dse.domain.model.SimulationScenario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SimulationScenarioRepository extends JpaRepository<SimulationScenario, Long> {

    List<SimulationScenario> findByUserIdOrderByCreatedAtDesc(Long userId);
}