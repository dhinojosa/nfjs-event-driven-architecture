package com.evolutionnext.application;

import com.evolutionnext.domain.OrderSubmitted;
import com.evolutionnext.port.in.order.ForSubscriberCommandPort;
import com.evolutionnext.port.out.payment.PaymentGateway;

public class ForSubscriberOrderApplicationService implements ForSubscriberCommandPort {
    private PaymentGateway paymentGateway;

    public void handleOrderEvent(OrderSubmitted orderSubmitted) {

    }
}
