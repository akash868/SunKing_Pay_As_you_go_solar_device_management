package com.sunking.payg.integration;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MobileMoneyRequest {
    private String phoneNumber;
    private BigDecimal amount;
    private String transactionReference;
    private String description;
    private String currency;
}
