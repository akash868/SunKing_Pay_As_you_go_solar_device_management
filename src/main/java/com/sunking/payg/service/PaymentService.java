package com.sunking.payg.service;

import com.sunking.payg.dto.request.CreatePaymentRequest;
import com.sunking.payg.dto.response.PaymentResponse;
import com.sunking.payg.entity.*;
import com.sunking.payg.exception.BusinessException;
import com.sunking.payg.exception.DuplicateResourceException;
import com.sunking.payg.exception.ResourceNotFoundException;
import com.sunking.payg.integration.MobileMoneyGatewayClient;
import com.sunking.payg.integration.MobileMoneyRequest;
import com.sunking.payg.integration.MobileMoneyResponse;
import com.sunking.payg.repository.DeviceAssignmentRepository;
import com.sunking.payg.repository.DeviceRepository;
import com.sunking.payg.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DeviceAssignmentRepository assignmentRepository;
    private final DeviceRepository deviceRepository;
    private final CustomerService customerService;
    private final MobileMoneyGatewayClient gatewayClient;

    /**
     * Core business logic: records a payment, calls the external gateway, and
     * updates device status accordingly.
     */
    @Transactional
    public PaymentResponse recordPayment(CreatePaymentRequest request) {
        // --- Idempotency check ---
        if (request.getTransactionReference() != null &&
                paymentRepository.existsByTransactionReference(request.getTransactionReference())) {
            throw new DuplicateResourceException("Payment with transaction reference '"
                    + request.getTransactionReference() + "' has already been processed");
        }

        Customer customer = customerService.getCustomerEntityById(request.getCustomerId());

        DeviceAssignment assignment = assignmentRepository.findByIdAndCustomerIdAndIsActiveTrue(
                request.getAssignmentId(), request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active assignment not found for customer and assignment ID provided"));

        if (assignment.getIsFullyPaid()) {
            throw new BusinessException("This device is already fully paid. No further payments required.");
        }

        BigDecimal remainingBalance = assignment.getRemainingBalance();
        if (request.getAmount().compareTo(remainingBalance) > 0) {
            throw new BusinessException(String.format(
                    "Payment amount (%.2f) exceeds remaining balance (%.2f). Please pay at most %.2f",
                    request.getAmount(), remainingBalance, remainingBalance));
        }

        // Generate transaction reference if not provided
        String txRef = (request.getTransactionReference() != null)
                ? request.getTransactionReference()
                : "TXN-" + UUID.randomUUID().toString().toUpperCase().replace("-", "").substring(0, 16);

        // Create payment record (PENDING)
        Payment payment = Payment.builder()
                .customer(customer)
                .assignment(assignment)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .mobileNumber(request.getMobileNumber())
                .transactionReference(txRef)
                .status(Payment.PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);

        // --- Call external gateway for mobile money payments ---
        if (request.getPaymentMethod() == Payment.PaymentMethod.MOBILE_MONEY) {
            MobileMoneyResponse gatewayResponse = gatewayClient.initiatePayment(
                    MobileMoneyRequest.builder()
                            .phoneNumber(request.getMobileNumber() != null ? request.getMobileNumber()
                                    : customer.getPhoneNumber())
                            .amount(request.getAmount())
                            .transactionReference(txRef)
                            .description("PAYG Payment - " + assignment.getDevice().getModel())
                            .currency("KES")
                            .build());

            if (gatewayResponse.isSuccess()) {
                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                payment.setExternalTransactionId(gatewayResponse.getTransactionId());
                payment.setProcessedAt(LocalDateTime.now());
            } else {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setFailureReason(gatewayResponse.getMessage());
                paymentRepository.save(payment);
                log.warn("Mobile money payment failed: ref={}, reason={}", txRef, gatewayResponse.getMessage());
                return toResponse(payment, assignment);
            }
        } else {
            // Cash / Bank transfer — mark success immediately
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setProcessedAt(LocalDateTime.now());
        }

        paymentRepository.save(payment);

        // --- Update assignment payment tracking ---
        BigDecimal newAmountPaid = assignment.getAmountPaid().add(request.getAmount());
        assignment.setAmountPaid(newAmountPaid);
        assignment.setLastPaymentDate(LocalDate.now());
        assignment.setDaysOverdue(0);

        // Calculate how many days forward this payment covers
        long daysCovered = request.getAmount()
                .divide(assignment.getDailyRateAtAssignment(), 0, java.math.RoundingMode.FLOOR)
                .longValue();

        LocalDate currentDue = assignment.getNextPaymentDueDate();
        LocalDate today = LocalDate.now();
        LocalDate baseDate = (currentDue != null && currentDue.isAfter(today)) ? currentDue : today;
        assignment.setNextPaymentDueDate(baseDate.plusDays(Math.max(1, daysCovered)));

        // Check if fully paid
        if (newAmountPaid.compareTo(assignment.getTotalCostAtAssignment()) >= 0) {
            assignment.setIsFullyPaid(true);
            assignment.setFullyPaidAt(LocalDateTime.now());
            assignment.getDevice().setStatus(Device.DeviceStatus.INACTIVE);
            log.info("Device fully paid: assignmentId={}, deviceId={}", assignment.getId(),
                    assignment.getDevice().getId());
        } else {
            // Re-activate the device if it was locked
            if (assignment.getDevice().getStatus() == Device.DeviceStatus.LOCKED) {
                assignment.getDevice().setStatus(Device.DeviceStatus.ACTIVE);
                assignment.getDevice().setLockedAt(null);
                deviceRepository.save(assignment.getDevice());
                log.info("Device reactivated after payment: deviceId={}", assignment.getDevice().getId());
            }
        }

        assignmentRepository.save(assignment);

        log.info("Payment recorded: id={}, amount={}, customer={}", payment.getId(), request.getAmount(),
                customer.getId());
        return toResponse(payment, assignment);
    }

    public Page<PaymentResponse> getPaymentsByCustomer(UUID customerId, Pageable pageable) {
        // Verify customer exists
        customerService.getCustomerEntityById(customerId);
        return paymentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                .map(p -> toResponse(p, p.getAssignment()));
    }

    public Page<PaymentResponse> getPaymentsByAssignment(UUID assignmentId, Pageable pageable) {
        return paymentRepository.findByAssignmentIdOrderByCreatedAtDesc(assignmentId, pageable)
                .map(p -> toResponse(p, p.getAssignment()));
    }

    private PaymentResponse toResponse(Payment p, DeviceAssignment assignment) {
        return PaymentResponse.builder()
                .id(p.getId())
                .customerId(p.getCustomer().getId())
                .customerName(p.getCustomer().getFirstName() + " " + p.getCustomer().getLastName())
                .assignmentId(assignment.getId())
                .deviceId(assignment.getDevice().getId())
                .deviceSerialNumber(assignment.getDevice().getSerialNumber())
                .amount(p.getAmount())
                .status(p.getStatus())
                .paymentMethod(p.getPaymentMethod())
                .transactionReference(p.getTransactionReference())
                .externalTransactionId(p.getExternalTransactionId())
                .mobileNumber(p.getMobileNumber())
                .failureReason(p.getFailureReason())
                .retryCount(p.getRetryCount())
                .processedAt(p.getProcessedAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
