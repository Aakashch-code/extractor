package com.example.extractor.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InvoiceExtractionResult(

        String invoiceNumber,
        String vendorName,
        String invoiceDate,
        String dueDate,
        String purchaseOrderNumber,
        String currency,
        String invoiceType,
        String paymentTerms,

        String vendorAddress,
        String vendorEmail,
        String vendorPhone,
        String vendorTaxId,
        String vendorRegistrationNumber,

        String customerName,
        String customerAddress,
        String customerEmail,
        String customerPhone,
        String customerTaxId,

        String shippingAddress,
        String shippingMethod,

        List<LineItem> items,

        BigDecimal subtotal,
        BigDecimal discountTotal,
        BigDecimal taxableAmount,
        BigDecimal taxAmount,
        BigDecimal shippingAmount,
        BigDecimal otherCharges,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        BigDecimal balanceDue,

        String paymentMethod,
        String bankName,
        String accountNumber,
        String iban,
        String swiftCode,
        String paymentReference,

        String notes,
        String termsAndConditions
) {
}