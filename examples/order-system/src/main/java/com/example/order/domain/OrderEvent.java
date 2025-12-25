package com.example.order.domain;

import java.time.LocalDateTime;

public record OrderEvent(
    String orderId,
    String customerId,
    OrderStatus status,
    String description,
    LocalDateTime timestamp
) {
    public static OrderEvent created(String orderId, String customerId) {
        return new OrderEvent(orderId, customerId, OrderStatus.CREATED,
                "주문이 생성되었습니다", LocalDateTime.now());
    }

    public static OrderEvent paid(String orderId, String customerId) {
        return new OrderEvent(orderId, customerId, OrderStatus.PAID,
                "결제가 완료되었습니다", LocalDateTime.now());
    }

    public static OrderEvent shipped(String orderId, String customerId) {
        return new OrderEvent(orderId, customerId, OrderStatus.SHIPPED,
                "배송이 시작되었습니다", LocalDateTime.now());
    }

    public static OrderEvent delivered(String orderId, String customerId) {
        return new OrderEvent(orderId, customerId, OrderStatus.DELIVERED,
                "배송이 완료되었습니다", LocalDateTime.now());
    }

    public static OrderEvent cancelled(String orderId, String customerId, String reason) {
        return new OrderEvent(orderId, customerId, OrderStatus.CANCELLED,
                "주문이 취소되었습니다: " + reason, LocalDateTime.now());
    }
}
