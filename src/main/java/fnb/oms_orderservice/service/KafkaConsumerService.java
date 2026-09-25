package fnb.oms_orderservice.service;

import fnb.oms_orderservice.entity.Order;
import fnb.oms_orderservice.entity.OrderStatus;
import fnb.oms_orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "delivery-completed", groupId = "order-management-group")
    @Transactional
    public void consumeDeliveryCompleted(String message) {
        // Simple manual parsing of JSON like {"orderId": 123}
        // In a real app, Jackson ObjectMapper would deserialize an event class
        Pattern pattern = Pattern.compile("\"orderId\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(message);
        
        if (matcher.find()) {
            Long orderId = Long.parseLong(matcher.group(1));
            
            Order order = orderRepository.findById(orderId).orElse(null);
            
            if (order != null) {
                // Idempotent consumer logic: guard against reprocessing (Section 7.1)
                if (order.getStatus() != OrderStatus.DELIVERED) {
                    order.setStatus(OrderStatus.DELIVERED);
                    orderRepository.save(order);
                }
            }
        }
    }
}
