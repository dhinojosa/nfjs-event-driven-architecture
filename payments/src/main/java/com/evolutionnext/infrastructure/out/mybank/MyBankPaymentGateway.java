package com.evolutionnext.infrastructure.out.mybank;


import com.evolutionnext.port.out.payment.PaymentGateway;

public class MyBankPaymentGateway implements PaymentGateway {
    @Override
    public PaymentResult processPayment(String orderId, String customerId, int amount) throws PaymentException {
        return null;
    }

    @Override
    public PaymentResult refundPayment(String transactionId, int amount) throws PaymentException {
        return null;
    }
}
