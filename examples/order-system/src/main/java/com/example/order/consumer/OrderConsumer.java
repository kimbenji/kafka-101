package com.example.order.consumer;

import com.example.order.domain.OrderEvent;
import com.example.order.domain.OrderStatus;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    @KafkaListener(topics = "order-events", groupId = "order-processor")
    public void consume(ConsumerRecord<String, OrderEvent> record) {
        OrderEvent event = record.value();

        log.info("========================================");
        log.info("이벤트 수신");
        log.info("  Partition: {}, Offset: {}", record.partition(), record.offset());
        log.info("  Key (OrderId): {}", record.key());
        log.info("  CustomerId: {}", event.customerId());
        log.info("  Status: {}", event.status());
        log.info("  Description: {}", event.description());
        log.info("  Timestamp: {}", event.timestamp());
        log.info("========================================");

        // 상태별 처리 로직
        processEvent(event);
    }

    private void processEvent(OrderEvent event) {
        switch (event.status()) {
            case CREATED -> handleOrderCreated(event);
            case PAID -> handleOrderPaid(event);
            case SHIPPED -> handleOrderShipped(event);
            case DELIVERED -> handleOrderDelivered(event);
            case CANCELLED -> handleOrderCancelled(event);
        }
    }

    private void handleOrderCreated(OrderEvent event) {
        log.info("[처리] 주문 생성 - 재고 확인 및 결제 대기");
        // 실제 구현: 재고 확인, 결제 요청 등
    }

    private void handleOrderPaid(OrderEvent event) {
        log.info("[처리] 결제 완료 - 배송 준비 시작");
        // 실제 구현: 배송 서비스에 배송 요청
    }

    private void handleOrderShipped(OrderEvent event) {
        log.info("[처리] 배송 시작 - 고객에게 알림 발송");
        // 실제 구현: 알림 서비스에 알림 요청
    }

    private void handleOrderDelivered(OrderEvent event) {
        log.info("[처리] 배송 완료 - 주문 완료 처리");
        // 실제 구현: 주문 상태 업데이트, 리뷰 요청
    }

    private void handleOrderCancelled(OrderEvent event) {
        log.info("[처리] 주문 취소 - 환불 처리 시작");
        // 실제 구현: 결제 취소, 재고 복구
    }
}
