package com.example.extractor.repository;

import com.example.extractor.model.InvoiceDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceDocument, String> {
}