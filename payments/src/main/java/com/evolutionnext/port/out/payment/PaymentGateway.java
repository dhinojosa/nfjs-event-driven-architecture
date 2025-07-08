package com.evolutionnext.port.out.payment;


public interface PaymentGateway {
    record PaymentResult(String transactionId, boolean successful) {
    }

    class PaymentException extends RuntimeException {
        public PaymentException(String message) {
            super(message);
        }
    }

    PaymentResult processPayment(String orderId, String customerId, int amount) throws PaymentException;

    PaymentResult refundPayment(String transactionId, int amount) throws PaymentException;
}
