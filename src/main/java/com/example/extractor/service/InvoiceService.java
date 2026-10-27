package com.example.extractor.service;

import com.example.extractor.model.ExtractionStatus;
import com.example.extractor.model.InvoiceDocument;
import com.example.extractor.model.InvoiceFile;
import com.example.extractor.repository.InvoiceFileRepository;
import com.example.extractor.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository repository;
    private final InvoiceFileRepository fileRepository;
    private final HumanVsAiAccuracyScorer accuracyScorer;

    public List<InvoiceDocument> getPendingInvoices() {
        return repository.findAll().stream()
                .filter(doc -> ExtractionStatus.PENDING_REVIEW.equals(doc.getStatus()))
                .toList();
    }

    public Optional<InvoiceDocument> getInvoiceById(String id) {
        return repository.findById(id);
    }

    public FileDownloadDto getInvoiceFile(String id) throws IOException {
        InvoiceDocument document = repository.findById(id).orElse(null);
        if (document == null || document.getFileId() == null) {
            return null;
        }

        InvoiceFile file = fileRepository.findById(document.getFileId()).orElse(null);
        if (file == null) {
            return null;
        }

        return new FileDownloadDto(
                new ByteArrayResource(file.getData()),
                file.getContentType(),
                file.getFilename()
        );
    }

    public Optional<InvoiceDocument> updateInvoice(String id, InvoiceDocument updatedData) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }

        // Ensure the ID cannot be overwritten by the payload
        updatedData.setId(id);
        return Optional.of(repository.save(updatedData));
    }

    public Optional<InvoiceDocument> approveInvoice(String id, String username, int userApprovalAuthorityLevel) {
        return repository.findById(id).map(existing -> {

            if (userApprovalAuthorityLevel <= existing.getCurrentApprovalLevel()) {
                throw new IllegalArgumentException("User level too low or invoice already approved at this level.");
            }

            existing.setCurrentApprovalLevel(userApprovalAuthorityLevel);

            if (existing.getCurrentApprovalLevel() >= existing.getRequiredApprovalLevel()) {
                existing.setStatus(ExtractionStatus.APPROVED);
                double finalScore = accuracyScorer.calculateAccuracy(existing, existing.getOriginalAiData());
                existing.setAiAccuracyScore(finalScore);
            } else {
                existing.setStatus(ExtractionStatus.PENDING_REVIEW);
            }

            existing.setReviewedBy(username);
            existing.setReviewedAt(LocalDateTime.now());

            return repository.save(existing);
        });
    }

    public record FileDownloadDto(Resource resource, String contentType, String filename) {}
}