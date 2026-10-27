package com.example.extractor.service;

import com.example.extractor.model.*;
import com.example.extractor.repository.InvoiceFileRepository;
import com.example.extractor.repository.InvoiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class InvoiceExtractionService {
    private static final List<String> CANDIDATE_MODELS = List.of(
            "gemini-3.5-flash-lite"
    );

    private final ChatClient chatClient;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceFileRepository invoiceFileRepository;
    private final InvoiceExtractionScorer scorer;
    private final ApprovalMatrixService approvalMatrixService;

    public InvoiceExtractionService(
            ChatClient.Builder chatClientBuilder,
            InvoiceRepository invoiceRepository,
            InvoiceFileRepository invoiceFileRepository,
            InvoiceExtractionScorer scorer,
            ApprovalMatrixService approvalMatrixService) {

        this.chatClient = chatClientBuilder.build();
        this.invoiceRepository = invoiceRepository;
        this.invoiceFileRepository = invoiceFileRepository;
        this.scorer = scorer;
        this.approvalMatrixService = approvalMatrixService;
    }

    public InvoiceDocument processAndSaveInvoice(MultipartFile file) throws IOException {
        validateFile(file);

        byte[] fileBytes = file.getBytes();
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        // Save file to Postgres instead of GridFS
        InvoiceFile invoiceFile = InvoiceFile.builder()
                .filename(originalFilename)
                .contentType(contentType)
                .data(fileBytes)
                .build();
        InvoiceFile savedFile = invoiceFileRepository.save(invoiceFile);

        log.info("Saved original file to Database with ID: {}", savedFile.getId());

        String prompt = buildPrompt();

        List<CompletableFuture<ScoredCandidate>> futures = CANDIDATE_MODELS.stream()
                .map(modelName -> CompletableFuture.supplyAsync(() -> {
                    try {
                        InvoiceExtractionResult result = extractWithModel(modelName, prompt, fileBytes, contentType, originalFilename);
                        return scorer.score(modelName, result);
                    } catch (Exception ex) {
                        log.error("Extraction failed for model: {}", modelName, ex);
                        return new ScoredCandidate(modelName, null, 0.0, java.util.Map.of("error", 0.0), false);
                    }
                }))
                .toList();

        List<ScoredCandidate> candidates = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        candidates.forEach(c -> log.info("Model: [{}] -> Total Score: {} | Breakdown: {}",
                c.modelName(), String.format("%.2f", c.totalScore()), c.scoreBreakdown()));

        ScoredCandidate winner = candidates.stream()
                .max(Comparator.comparingDouble(ScoredCandidate::totalScore))
                .orElseThrow(() -> new IllegalStateException("No valid extraction candidates found"));

        if (winner.extraction() == null) {
            throw new IllegalStateException("All models failed to extract structured invoice data.");
        }

        InvoiceDocument document = mapToDocument(winner.extraction(), savedFile.getId());
        InvoiceDocument savedDocument = invoiceRepository.save(document);

        log.info("Invoice saved successfully with Database ID: {}", savedDocument.getId());
        return savedDocument;
    }

    private InvoiceExtractionResult extractWithModel(String modelName, String prompt, byte[] bytes, String contentType, String filename) {
        ByteArrayResource resource = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() { return filename; }
        };

        Media media = new Media(MimeTypeUtils.parseMimeType(contentType), resource);

        return chatClient.prompt()
                .options(ChatOptions.builder().model(modelName).temperature(0.0).build())
                .user(u -> u.text(prompt).media(media))
                .call()
                .entity(InvoiceExtractionResult.class);
    }

    private void validateFile(MultipartFile file) {
        // ... (Keep existing validation logic identical) ...
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Invoice file is empty");
        String contentType = file.getContentType();
        if (contentType == null) throw new IllegalArgumentException("Could not determine file type");
        boolean isPdf = contentType.equalsIgnoreCase("application/pdf");
        boolean isImage = contentType.equalsIgnoreCase("image/jpeg") || contentType.equalsIgnoreCase("image/png") || contentType.equalsIgnoreCase("image/webp");
        if (!isPdf && !isImage) throw new IllegalArgumentException("Unsupported file type: " + contentType);
    }

    private InvoiceDocument mapToDocument(InvoiceExtractionResult data, String fileId) {
        int requiredLevel = approvalMatrixService.calculateRequiredLevel(data.totalAmount());

        return InvoiceDocument.builder()
                .fileId(fileId)
                .status(ExtractionStatus.PENDING_REVIEW)
                .originalAiData(data)
                .requiredApprovalLevel(requiredLevel)
                .currentApprovalLevel(0)
                .invoiceNumber(data.invoiceNumber()) // Assigned to property, not ID
                .invoiceDate(data.invoiceDate())
                .dueDate(data.dueDate())
                .purchaseOrderNumber(data.purchaseOrderNumber())
                .currency(data.currency())
                // ... map all remaining fields exactly as you did before ...
                .vendorName(data.vendorName())
                .vendorAddress(data.vendorAddress())
                .vendorEmail(data.vendorEmail())
                .vendorPhone(data.vendorPhone())
                .vendorTaxId(data.vendorTaxId())
                .vendorRegistrationNumber(data.vendorRegistrationNumber())
                .customerName(data.customerName())
                .customerAddress(data.customerAddress())
                .customerEmail(data.customerEmail())
                .customerPhone(data.customerPhone())
                .customerTaxId(data.customerTaxId())
                .shippingAddress(data.shippingAddress())
                .shippingMethod(data.shippingMethod())
                .items(data.items())
                .subtotal(data.subtotal())
                .discountTotal(data.discountTotal())
                .taxableAmount(data.taxableAmount())
                .taxAmount(data.taxAmount())
                .shippingAmount(data.shippingAmount())
                .otherCharges(data.otherCharges())
                .totalAmount(data.totalAmount())
                .amountPaid(data.amountPaid())
                .balanceDue(data.balanceDue())
                .paymentMethod(data.paymentMethod())
                .bankName(data.bankName())
                .accountNumber(data.accountNumber())
                .iban(data.iban())
                .swiftCode(data.swiftCode())
                .paymentReference(data.paymentReference())
                .notes(data.notes())
                .termsAndConditions(data.termsAndConditions())
                .build();
    }

    private String buildPrompt() {
        return """
                You are an expert AI invoice extraction system.
                Analyze the attached invoice carefully.
                If the invoice contains multiple pages, treat all pages as one single invoice document.

                IMPORTANT RULES:
                1. Never guess or invent information.
                2. If a field is missing, return null.
                3. If a field is present but unreadable, return null.
                4. Extract every visible line item.
                5. Preserve values accurately.
                6. Convert dates to YYYY-MM-DD.
                7. Return monetary values as numbers only (no currency symbols).
                8. Identify the currency separately.
                9. Preserve decimal precision where visible.
                10. Return only structured data matching the requested schema.
                """;
    }
}