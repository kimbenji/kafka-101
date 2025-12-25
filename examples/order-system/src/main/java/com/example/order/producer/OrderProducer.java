package com.example.order.producer;

import com.example.order.domain.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderProducer.class);
    private static final String TOPIC = "order-events";

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public OrderProducer(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 주문 이벤트를 발행합니다.
     * orderId를 Key로 사용하여 동일 주문의 이벤트가 순서대로 처리되도록 합니다.
     */
    public void publish(OrderEvent event) {
        log.info("이벤트 발행 - OrderId: {}, Status: {}", event.orderId(), event.status());

        kafkaTemplate.send(TOPIC, event.orderId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("발행 성공 - Partition: {}, Offset: {}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("발행 실패 - OrderId: {}", event.orderId(), ex);
                    }
                });
    }
}
