package com.sunking.payg.controller;

import com.sunking.payg.dto.request.CreatePaymentRequest;
import com.sunking.payg.dto.response.ApiResponse;
import com.sunking.payg.dto.response.PaymentResponse;
import com.sunking.payg.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "PAYG payment management endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Record a payment", description = "Record a PAYG payment for a device assignment. " +
            "For MOBILE_MONEY payments, this triggers the external payment gateway. " +
            "Provide a unique transactionReference for idempotency.")
    public ResponseEntity<ApiResponse<PaymentResponse>> recordPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.recordPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment recorded", response));
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Get payment history", description = "Paginated payment history for a customer, sorted by most recent first")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByCustomer(
            @PathVariable UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<PaymentResponse> result = paymentService.getPaymentsByCustomer(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.paginated(result));
    }

    @GetMapping("/assignment/{assignmentId}")
    @Operation(summary = "Get payments for a specific device assignment")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByAssignment(
            @PathVariable UUID assignmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<PaymentResponse> result = paymentService.getPaymentsByAssignment(assignmentId, pageable);
        return ResponseEntity.ok(ApiResponse.paginated(result));
    }
}
