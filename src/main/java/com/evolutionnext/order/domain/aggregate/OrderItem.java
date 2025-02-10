package com.evolutionnext.order.domain.aggregate;


public record OrderItem(ProductId productId, int quantity, int price) {
}
