package com.example.extractor.model;

import java.util.Map;

public record ScoredCandidate(
        String modelName,
        InvoiceExtractionResult extraction,
        double totalScore,
        Map<String, Double> scoreBreakdown,
        boolean isMathematicallySound
) {}