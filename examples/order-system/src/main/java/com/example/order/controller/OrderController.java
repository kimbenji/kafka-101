package com.example.order.controller;

import com.example.order.domain.OrderEvent;
import com.example.order.producer.OrderProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderProducer orderProducer;

    public OrderController(OrderProducer orderProducer) {
        this.orderProducer = orderProducer;
    }

    /**
     * 새 주문 생성
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createOrder(@RequestBody CreateOrderRequest request) {
        String orderId = UUID.randomUUID().toString().substring(0, 8);

        orderProducer.publish(OrderEvent.created(orderId, request.customerId()));

        return ResponseEntity.ok(Map.of(
                "orderId", orderId,
                "message", "주문이 생성되었습니다"
        ));
    }

    /**
     * 결제 완료 처리
     */
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<Map<String, String>> payOrder(
            @PathVariable String orderId,
            @RequestParam String customerId) {

        orderProducer.publish(OrderEvent.paid(orderId, customerId));

        return ResponseEntity.ok(Map.of(
                "orderId", orderId,
                "message", "결제가 완료되었습니다"
        ));
    }

    /**
     * 배송 시작
     */
    @PostMapping("/{orderId}/ship")
    public ResponseEntity<Map<String, String>> shipOrder(
            @PathVariable String orderId,
            @RequestParam String customerId) {

        orderProducer.publish(OrderEvent.shipped(orderId, customerId));

        return ResponseEntity.ok(Map.of(
                "orderId", orderId,
                "message", "배송이 시작되었습니다"
        ));
    }

    /**
     * 배송 완료
     */
    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<Map<String, String>> deliverOrder(
            @PathVariable String orderId,
            @RequestParam String customerId) {

        orderProducer.publish(OrderEvent.delivered(orderId, customerId));

        return ResponseEntity.ok(Map.of(
                "orderId", orderId,
                "message", "배송이 완료되었습니다"
        ));
    }

    /**
     * 주문 취소
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, String>> cancelOrder(
            @PathVariable String orderId,
            @RequestParam String customerId,
            @RequestParam(defaultValue = "고객 요청") String reason) {

        orderProducer.publish(OrderEvent.cancelled(orderId, customerId, reason));

        return ResponseEntity.ok(Map.of(
                "orderId", orderId,
                "message", "주문이 취소되었습니다"
        ));
    }

    record CreateOrderRequest(String customerId) {}
}
