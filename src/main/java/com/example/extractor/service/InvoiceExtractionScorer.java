package com.example.extractor.service;

import com.example.extractor.model.ScoredCandidate;
import com.example.extractor.model.InvoiceExtractionResult;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class InvoiceExtractionScorer {

    private static final double EPSILON = 0.05;

    public ScoredCandidate score(String modelName, InvoiceExtractionResult res) {
        if (res == null) {
            return new ScoredCandidate(modelName, null, 0.0, Map.of("error", 0.0), false);
        }

        Map<String, Double> breakdown = new LinkedHashMap<>();

        double headerScore = 0.0;
        if (res.invoiceNumber() != null && !res.invoiceNumber().isBlank()) headerScore += 0.07;
        if (res.invoiceDate() != null && res.invoiceDate().matches("\\d{4}-\\d{2}-\\d{2}")) headerScore += 0.06;
        if (res.vendorName() != null && !res.vendorName().isBlank()) headerScore += 0.06;
        if (res.currency() != null && !res.currency().isBlank()) headerScore += 0.06;
        breakdown.put("headersCompleteness", headerScore);

        double lineItemScore = 0.0;
        BigDecimal itemsSum = BigDecimal.ZERO;
        boolean hasValidItems = res.items() != null && !res.items().isEmpty();

        if (hasValidItems) {
            lineItemScore += 0.15; // Extracted at least one item
            boolean allItemsValid = true;
            for (var item : res.items()) {
                if (item.lineTotal() != null) {
                    itemsSum = itemsSum.add(item.lineTotal());
                } else {
                    allItemsValid = false;
                }
            }
            if (allItemsValid) lineItemScore += 0.20;
        }
        breakdown.put("lineItemsScore", lineItemScore);

        double mathScore = 0.0;
        boolean mathSound = false;

        BigDecimal subtotal = res.subtotal();
        BigDecimal tax = res.taxAmount() != null ? res.taxAmount() : BigDecimal.ZERO;
        BigDecimal discount = res.discountTotal() != null ? res.discountTotal() : BigDecimal.ZERO;
        BigDecimal shipping = res.shippingAmount() != null ? res.shippingAmount() : BigDecimal.ZERO;
        BigDecimal total = res.totalAmount();

        if (subtotal != null && hasValidItems && itemsSum.compareTo(BigDecimal.ZERO) > 0) {
            if (itemsSum.subtract(subtotal).abs().doubleValue() <= EPSILON) {
                mathScore += 0.20;
            }
        }

        if (subtotal != null && total != null) {
            BigDecimal expectedTotal = subtotal.add(tax).add(shipping).subtract(discount);
            if (expectedTotal.subtract(total).abs().doubleValue() <= EPSILON) {
                mathScore += 0.20;
                mathSound = true;
            }
        } else if (total != null && total.compareTo(BigDecimal.ZERO) > 0) {
            mathScore += 0.05;
        }
        breakdown.put("mathIntegrity", mathScore);

        double totalScore = (headerScore + lineItemScore + mathScore) * 100.0;

        return new ScoredCandidate(modelName, res, totalScore, breakdown, mathSound);
    }
}