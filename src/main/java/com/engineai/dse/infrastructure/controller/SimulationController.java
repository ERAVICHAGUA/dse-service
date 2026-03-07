package com.engineai.dse.infrastructure.controller;

import com.engineai.dse.application.service.SimulationService;
import com.engineai.dse.domain.model.SimulationResult;
import com.engineai.dse.domain.model.SimulationScenario;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/simulations")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SimulationResult simulate(@RequestBody CreateSimulationRequest request) {
        return simulationService.simulate(request);
    }

    @GetMapping("/results")
    public List<SimulationResult> listResults(@RequestParam Long userId) {
        return simulationService.findResultsByUserId(userId);
    }

    @GetMapping("/scenarios")
    public List<SimulationScenario> listScenarios(@RequestParam Long userId) {
        return simulationService.findScenariosByUserId(userId);
    }
}