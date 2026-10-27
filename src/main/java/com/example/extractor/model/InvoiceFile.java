package com.example.extractor.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "invoice_files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String filename;
    private String contentType;

    // Maps to PostgreSQL 'bytea' natively
    private byte[] data;
}