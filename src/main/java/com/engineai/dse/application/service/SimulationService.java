package com.engineai.dse.application.service;

import com.engineai.dse.domain.model.SimulationResult;
import com.engineai.dse.domain.model.SimulationScenario;
import com.engineai.dse.domain.repository.SimulationResultRepository;
import com.engineai.dse.domain.repository.SimulationScenarioRepository;
import com.engineai.dse.infrastructure.controller.CreateSimulationRequest;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.List;

@Service
public class SimulationService {

    private final SimulationScenarioRepository simulationScenarioRepository;
    private final SimulationResultRepository simulationResultRepository;

    @Value("${services.tiie.base-url}")
    private String tiieBaseUrl;

    @Value("${services.crfe.base-url}")
    private String crfeBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public SimulationService(
            SimulationScenarioRepository simulationScenarioRepository,
            SimulationResultRepository simulationResultRepository
    ) {
        this.simulationScenarioRepository = simulationScenarioRepository;
        this.simulationResultRepository = simulationResultRepository;
    }

    public SimulationResult simulate(CreateSimulationRequest request) {
        String scenarioType = normalize(request.scenarioType());

        SimulationScenario scenario = SimulationScenario.builder()
                .userId(request.userId())
                .scenarioType(scenarioType)
                .scenarioName(request.scenarioName())
                .inputPayload(buildInputPayload(request))
                .status("SIMULATED")
                .build();

        SimulationScenario savedScenario = simulationScenarioRepository.save(scenario);

        BigDecimal projectedIncome = BigDecimal.ZERO;
        BigDecimal projectedExpense = BigDecimal.ZERO;
        BigDecimal projectedBalance = BigDecimal.ZERO;
        String recommendation = "Escenario procesado correctamente.";

        switch (scenarioType) {
            case "SAVINGS" -> {
                BigDecimal monthlySaving = defaultAmount(request.monthlySaving());
                int months = defaultMonths(request.months());
                projectedBalance = monthlySaving.multiply(BigDecimal.valueOf(months));
                recommendation = projectedBalance.compareTo(BigDecimal.ZERO) > 0
                        ? "El ahorro proyectado es viable."
                        : "El escenario de ahorro no genera impacto positivo.";
            }
            case "EXPENSE" -> {
                BigDecimal expenseAmount = defaultAmount(request.expenseAmount());
                projectedExpense = expenseAmount;
                projectedBalance = expenseAmount.negate();
                recommendation = projectedBalance.compareTo(new BigDecimal("-500")) < 0
                        ? "Conviene postergar este gasto."
                        : "El gasto es manejable, pero reduce tu liquidez.";
            }
            case "LOAN" -> {
                BigDecimal loanAmount = defaultAmount(request.loanAmount());
                int months = defaultMonths(request.months());
                projectedIncome = loanAmount;
                projectedExpense = loanAmount.divide(BigDecimal.valueOf(months), 2, java.math.RoundingMode.HALF_UP);
                projectedBalance = projectedIncome.subtract(projectedExpense);
                recommendation = "Evalúa la cuota mensual antes de tomar el préstamo.";
            }
            default -> {
                recommendation = "Tipo de escenario no reconocido. Se aplicó simulación base.";
            }
        }

        String riskLevel = resolveRisk(projectedBalance);

        SimulationResult result = SimulationResult.builder()
                .scenario(savedScenario)
                .userId(request.userId())
                .projectedBalance(projectedBalance)
                .projectedIncome(projectedIncome)
                .projectedExpense(projectedExpense)
                .riskLevel(riskLevel)
                .recommendation(recommendation)
                .resultPayload(buildResultPayload(projectedIncome, projectedExpense, projectedBalance, riskLevel))
                .build();

        return simulationResultRepository.save(result);
    }

    public List<SimulationResult> findResultsByUserId(Long userId) {
        return simulationResultRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<SimulationScenario> findScenariosByUserId(Long userId) {
        return simulationScenarioRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    private String normalize(String value) {
        return value == null ? "UNKNOWN" : value.trim().toUpperCase();
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private int defaultMonths(Integer months) {
        return months == null || months <= 0 ? 1 : months;
    }

    private String resolveRisk(BigDecimal projectedBalance) {
        if (projectedBalance.compareTo(BigDecimal.ZERO) < 0) {
            return "HIGH";
        }
        if (projectedBalance.compareTo(new BigDecimal("100.00")) < 0) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String buildInputPayload(CreateSimulationRequest request) {
        return """
                {
                  "monthlySaving": %s,
                  "expenseAmount": %s,
                  "loanAmount": %s,
                  "months": %s
                }
                """.formatted(
                defaultAmount(request.monthlySaving()),
                defaultAmount(request.expenseAmount()),
                defaultAmount(request.loanAmount()),
                request.months() == null ? 0 : request.months()
        );
    }

    private String buildResultPayload(
            BigDecimal projectedIncome,
            BigDecimal projectedExpense,
            BigDecimal projectedBalance,
            String riskLevel
    ) {
        return """
                {
                  "projectedIncome": %s,
                  "projectedExpense": %s,
                  "projectedBalance": %s,
                  "riskLevel": "%s"
                }
                """.formatted(
                projectedIncome,
                projectedExpense,
                projectedBalance,
                riskLevel
        );
    }
}