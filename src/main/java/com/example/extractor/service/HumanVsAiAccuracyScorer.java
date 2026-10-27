package com.example.extractor.service;

import com.example.extractor.model.InvoiceDocument;
import com.example.extractor.model.InvoiceExtractionResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component
public class HumanVsAiAccuracyScorer {

    public double calculateAccuracy(InvoiceDocument finalHumanDoc, InvoiceExtractionResult originalAi) {
        if (originalAi == null) return 100.0;

        int[] stats = new int[]{0, 0};

        compare(finalHumanDoc.getInvoiceNumber(), originalAi.invoiceNumber(), stats);
        compare(finalHumanDoc.getInvoiceDate(), originalAi.invoiceDate(), stats);
        compare(finalHumanDoc.getDueDate(), originalAi.dueDate(), stats);
        compare(finalHumanDoc.getPurchaseOrderNumber(), originalAi.purchaseOrderNumber(), stats);
        compare(finalHumanDoc.getCurrency(), originalAi.currency(), stats);
        compare(finalHumanDoc.getInvoiceType(), originalAi.invoiceType(), stats);
        compare(finalHumanDoc.getPaymentTerms(), originalAi.paymentTerms(), stats);

        compare(finalHumanDoc.getVendorName(), originalAi.vendorName(), stats);
        compare(finalHumanDoc.getVendorAddress(), originalAi.vendorAddress(), stats);
        compare(finalHumanDoc.getVendorEmail(), originalAi.vendorEmail(), stats);
        compare(finalHumanDoc.getVendorPhone(), originalAi.vendorPhone(), stats);
        compare(finalHumanDoc.getVendorTaxId(), originalAi.vendorTaxId(), stats);
        compare(finalHumanDoc.getVendorRegistrationNumber(), originalAi.vendorRegistrationNumber(), stats);

        compare(finalHumanDoc.getCustomerName(), originalAi.customerName(), stats);
        compare(finalHumanDoc.getCustomerAddress(), originalAi.customerAddress(), stats);
        compare(finalHumanDoc.getCustomerEmail(), originalAi.customerEmail(), stats);
        compare(finalHumanDoc.getCustomerPhone(), originalAi.customerPhone(), stats);
        compare(finalHumanDoc.getCustomerTaxId(), originalAi.customerTaxId(), stats);

        compare(finalHumanDoc.getShippingAddress(), originalAi.shippingAddress(), stats);
        compare(finalHumanDoc.getShippingMethod(), originalAi.shippingMethod(), stats);

        compare(finalHumanDoc.getPaymentMethod(), originalAi.paymentMethod(), stats); 
        compare(finalHumanDoc.getBankName(), originalAi.bankName(), stats);
        compare(finalHumanDoc.getAccountNumber(), originalAi.accountNumber(), stats);
        compare(finalHumanDoc.getIban(), originalAi.iban(), stats);
        compare(finalHumanDoc.getSwiftCode(), originalAi.swiftCode(), stats);
        compare(finalHumanDoc.getPaymentReference(), originalAi.paymentReference(), stats);

        compare(finalHumanDoc.getNotes(), originalAi.notes(), stats);
        compare(finalHumanDoc.getTermsAndConditions(), originalAi.termsAndConditions(), stats);
        compareNumbers(finalHumanDoc.getSubtotal(), originalAi.subtotal(), stats);
        compareNumbers(finalHumanDoc.getTaxAmount(), originalAi.taxAmount(), stats);
        compareNumbers(finalHumanDoc.getTotalAmount(), originalAi.totalAmount(), stats);
        compareNumbers(finalHumanDoc.getDiscountTotal(), originalAi.discountTotal(), stats);
        compareNumbers(finalHumanDoc.getShippingAmount(), originalAi.shippingAmount(), stats);
        compareNumbers(finalHumanDoc.getAmountPaid(), originalAi.amountPaid(), stats);
        compareNumbers(finalHumanDoc.getBalanceDue(), originalAi.balanceDue(), stats);

        compare(finalHumanDoc.getItems(), originalAi.items(), stats);

        int totalEntities = stats[0];
        int correctAiEntities = stats[1];

        if (totalEntities == 0) return 100.0;

        return ((double) correctAiEntities / totalEntities) * 100.0;
    }

    private void compare(Object humanVal, Object aiVal, int[] stats) {

        if (humanVal == null && aiVal == null) return;

        stats[0]++;

        if (Objects.equals(humanVal, aiVal)) {
            stats[1]++;
        }
    }

    private void compareNumbers(BigDecimal humanVal, BigDecimal aiVal, int[] stats) {
        if (humanVal == null && aiVal == null) return;

        stats[0]++;

        if (humanVal != null && aiVal != null) {

            if (humanVal.compareTo(aiVal) == 0) {
                stats[1]++;
            }
        }
    }
}