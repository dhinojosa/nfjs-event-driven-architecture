package com.evolutionnext.order.domain.aggregate;


public class CustomerId {
   private final String value;

    public CustomerId(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
