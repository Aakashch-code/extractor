package com.example.extractor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoices")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvoiceDocument {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private String id; // DB Primary Key

        private String invoiceNumber; // The extracted invoice number
        private String fileId;

        // Maker-Checker Fields
        @Enumerated(EnumType.STRING)
        private ExtractionStatus status;
        private String reviewedBy;
        private LocalDateTime reviewedAt;

        private Double aiAccuracyScore;

        // Maps the nested record directly to a PostgreSQL JSONB column
        @JdbcTypeCode(SqlTypes.JSON)
        @Column(columnDefinition = "jsonb")
        private InvoiceExtractionResult originalAiData;

        private String invoiceDate;
        private String dueDate;
        private String purchaseOrderNumber;
        private String currency;
        private String invoiceType;
        private String paymentTerms;

        private String vendorName;
        private String vendorAddress;
        private String vendorEmail;
        private String vendorPhone;
        private String vendorTaxId;
        private String vendorRegistrationNumber;

        private String customerName;
        private String customerAddress;
        private String customerEmail;
        private String customerPhone;
        private String customerTaxId;

        private String shippingAddress;
        private String shippingMethod;

        // Maps the list of records to a JSONB array
        @JdbcTypeCode(SqlTypes.JSON)
        @Column(columnDefinition = "jsonb")
        private List<LineItem> items;

        private BigDecimal subtotal;
        private BigDecimal discountTotal;
        private BigDecimal taxableAmount;
        private BigDecimal taxAmount;
        private BigDecimal shippingAmount;
        private BigDecimal otherCharges;
        private BigDecimal totalAmount;
        private BigDecimal amountPaid;
        private BigDecimal balanceDue;

        private String paymentMethod;
        private String bankName;
        private String accountNumber;
        private String iban;
        private String swiftCode;
        private String paymentReference;

        @Column(columnDefinition = "text")
        private String notes;

        @Column(columnDefinition = "text")
        private String termsAndConditions;

        private Integer requiredApprovalLevel;
        private Integer currentApprovalLevel;
}