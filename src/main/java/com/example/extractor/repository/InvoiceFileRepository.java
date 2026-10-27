package com.example.extractor.repository;

import com.example.extractor.model.InvoiceFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceFileRepository extends JpaRepository<InvoiceFile, String> {
}