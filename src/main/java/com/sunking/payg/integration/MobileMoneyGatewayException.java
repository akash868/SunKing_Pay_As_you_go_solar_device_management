package com.sunking.payg.integration;

public class MobileMoneyGatewayException extends RuntimeException {
    public MobileMoneyGatewayException(String message) {
        super(message);
    }

    public MobileMoneyGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
