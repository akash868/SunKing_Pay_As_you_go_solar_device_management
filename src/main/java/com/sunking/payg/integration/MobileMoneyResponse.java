package com.sunking.payg.integration;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobileMoneyResponse {
    private boolean success;
    private String transactionId;
    private String status;
    private String message;
    private String errorCode;
}
