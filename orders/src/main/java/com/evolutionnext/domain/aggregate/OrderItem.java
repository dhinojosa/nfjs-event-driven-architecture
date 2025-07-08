package com.evolutionnext.domain.aggregate;


public record OrderItem(ProductId productId, int quantity, int price) {
}
