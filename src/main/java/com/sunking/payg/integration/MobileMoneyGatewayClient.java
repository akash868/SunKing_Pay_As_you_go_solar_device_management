package com.sunking.payg.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

/**
 * Mock implementation of the Mobile Money Payment Gateway.
 *
 * In a real system this would make an HTTP call to an external provider
 * (e.g., M-Pesa Daraja API, Airtel Money, MTN MoMo).
 *
 * This mock simulates:
 * - 80% success rate
 * - Random latency (network simulation)
 * - Automatic retry with exponential backoff on transient failures
 */
@Component
@Slf4j
public class MobileMoneyGatewayClient {

    @Value("${integration.mobile-money.success-rate:0.8}")
    private double successRate;

    @Value("${integration.mobile-money.mock-enabled:true}")
    private boolean mockEnabled;

    private final Random random = new Random();

    /**
     * Initiates a mobile money payment with retry support.
     * Retries up to 3 times on RuntimeException with exponential backoff.
     */
    @Retryable(retryFor = {
            MobileMoneyGatewayException.class }, maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2.0, maxDelay = 5000))
    public MobileMoneyResponse initiatePayment(MobileMoneyRequest request) {
        log.info("Initiating mobile money payment: ref={}, amount={}, phone={}",
                request.getTransactionReference(), request.getAmount(), request.getPhoneNumber());

        if (mockEnabled) {
            return simulatePayment(request);
        }

        // TODO: Replace with actual HTTP client call to payment provider
        throw new UnsupportedOperationException("Real gateway not configured");
    }

    private MobileMoneyResponse simulatePayment(MobileMoneyRequest request) {
        // Simulate network latency (50–200ms)
        try {
            Thread.sleep(50 + random.nextInt(150));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate a transient network issue (10% chance)
        if (random.nextDouble() < 0.10) {
            log.warn("Simulating transient gateway error for ref={}", request.getTransactionReference());
            throw new MobileMoneyGatewayException("Transient gateway error - will retry");
        }

        // Simulate successful or failed payment
        boolean succeeded = random.nextDouble() < successRate;

        if (succeeded) {
            String transactionId = "MM-" + UUID.randomUUID().toString().toUpperCase().replace("-", "").substring(0, 12);
            log.info("Mobile money payment SUCCESS: ref={}, externalId={}", request.getTransactionReference(),
                    transactionId);
            return MobileMoneyResponse.builder()
                    .success(true)
                    .transactionId(transactionId)
                    .status("COMPLETED")
                    .message("Payment processed successfully")
                    .build();
        } else {
            log.warn("Mobile money payment FAILED: ref={}", request.getTransactionReference());
            return MobileMoneyResponse.builder()
                    .success(false)
                    .status("FAILED")
                    .message("Insufficient funds or invalid account")
                    .errorCode("INSUFFICIENT_FUNDS")
                    .build();
        }
    }

    /**
     * Recovery method called when all retries are exhausted.
     */
    @Recover
    public MobileMoneyResponse recoverPayment(MobileMoneyGatewayException ex, MobileMoneyRequest request) {
        log.error("All retry attempts exhausted for mobile money payment: ref={}", request.getTransactionReference());
        return MobileMoneyResponse.builder()
                .success(false)
                .status("FAILED")
                .message("Gateway unavailable after multiple retries: " + ex.getMessage())
                .errorCode("GATEWAY_TIMEOUT")
                .build();
    }
}
