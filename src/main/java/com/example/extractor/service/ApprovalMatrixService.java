package com.example.extractor.service;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class ApprovalMatrixService {

    public int calculateRequiredLevel(BigDecimal totalAmount) {
        if (totalAmount == null) return 1;

        double amount = totalAmount.doubleValue();

        if (amount >= 100000) {
            return 3;
        } else if (amount >= 50000) {
            return 2;
        } else {
            return 1;
        }
    }
}