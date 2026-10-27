package com.example.extractor.controller;

import com.example.extractor.model.InvoiceDocument;
import com.example.extractor.service.InvoiceExtractionService;
import com.example.extractor.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Invoice Management", description = "Maker-Checker endpoints for AI invoice extraction & verification")
public class InvoiceController {

    private final InvoiceExtractionService extractionService;
    private final InvoiceService invoiceService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Maker: Upload invoice (PDF/Image) for multi-model AI extraction and store in GridFS")
    public ResponseEntity<InvoiceDocument> uploadInvoice(
            @Parameter(description = "Invoice file (PDF/JPEG/PNG)", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file) {
        try {
            InvoiceDocument savedInvoice = extractionService.processAndSaveInvoice(file);
            return ResponseEntity.ok(savedInvoice);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/pending")
    @Operation(summary = "Checker: Fetch all invoices pending review")
    public ResponseEntity<List<InvoiceDocument>> getPendingInvoices() {
        return ResponseEntity.ok(invoiceService.getPendingInvoices());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Checker: Fetch extracted invoice data by ID")
    public ResponseEntity<InvoiceDocument> getInvoiceById(@PathVariable String id) {
        return invoiceService.getInvoiceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/{id}/file")
    @Operation(summary = "Checker: Stream original file inline for side-by-side frontend preview")
    public ResponseEntity<Resource> getInvoiceFile(@PathVariable String id) throws IOException {
        InvoiceService.FileDownloadDto fileDto = invoiceService.getInvoiceFile(id);

        if (fileDto == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileDto.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileDto.filename() + "\"")
                .body(fileDto.resource());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Checker: Update invoice with human corrections")
    public ResponseEntity<InvoiceDocument> updateInvoice(
            @PathVariable String id,
            @RequestBody InvoiceDocument updatedData) {
        return invoiceService.updateInvoice(id, updatedData)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Checker: Mark invoice APPROVED and calculate AI Accuracy Score")
    public ResponseEntity<InvoiceDocument> approveInvoice(
            @PathVariable String id,
            @RequestParam(defaultValue = "system-checker") String username, @RequestParam int userLevel) {
        return invoiceService.approveInvoice(id, username,userLevel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}