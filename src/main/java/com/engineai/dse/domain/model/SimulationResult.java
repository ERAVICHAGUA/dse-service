package com.engineai.dse.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "simulation_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    private SimulationScenario scenario;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "projected_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal projectedBalance;

    @Column(name = "projected_income", nullable = false, precision = 12, scale = 2)
    private BigDecimal projectedIncome;

    @Column(name = "projected_expense", nullable = false, precision = 12, scale = 2)
    private BigDecimal projectedExpense;

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel;

    @Column(name = "recommendation", length = 255)
    private String recommendation;

    @Column(name = "result_payload", columnDefinition = "json")
    private String resultPayload;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}