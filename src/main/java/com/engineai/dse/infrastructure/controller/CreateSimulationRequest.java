package com.engineai.dse.infrastructure.controller;

import java.math.BigDecimal;

public record CreateSimulationRequest(
        Long userId,
        String scenarioType,
        String scenarioName,
        BigDecimal monthlySaving,
        BigDecimal expenseAmount,
        BigDecimal loanAmount,
        Integer months
) {
}