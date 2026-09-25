package fnb.oms_orderservice.service;

import fnb.oms_orderservice.dto.OrderItemDTO;
import fnb.oms_orderservice.dto.OrderRequestDTO;
import fnb.oms_orderservice.dto.OrderResponseDTO;
import fnb.oms_orderservice.entity.InventoryItem;
import fnb.oms_orderservice.entity.Order;
import fnb.oms_orderservice.entity.OrderItem;
import fnb.oms_orderservice.entity.OrderStatus;
import fnb.oms_orderservice.repository.InventoryItemRepository;
import fnb.oms_orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        Order order = new Order();
        order.setCustomerId(requestDTO.getCustomerId());
        order.setDeliveryNeeded(requestDTO.getDeliveryNeeded() != null ? requestDTO.getDeliveryNeeded() : false);
        
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (requestDTO.getItems() != null) {
            for (OrderItemDTO itemDto : requestDTO.getItems()) {
                InventoryItem inventoryItem = inventoryItemRepository.findById(itemDto.getItemId())
                        .orElseThrow(() -> new RuntimeException("Inventory Item not found: " + itemDto.getItemId()));
                
                if (inventoryItem.getStockQuantity() < itemDto.getQuantity()) {
                    throw new RuntimeException("Not enough stock for item: " + inventoryItem.getItemName());
                }

                // Deduct stock
                inventoryItem.setStockQuantity(inventoryItem.getStockQuantity() - itemDto.getQuantity());
                inventoryItemRepository.save(inventoryItem); // Saves and increments version for optimistic locking

                BigDecimal unitPrice = inventoryItem.getPrice();
                BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity()));

                OrderItem orderItem = new OrderItem();
                orderItem.setInventoryItem(inventoryItem);
                orderItem.setQuantity(itemDto.getQuantity());
                orderItem.setUnitPriceAtPurchase(unitPrice);
                orderItem.setSubtotal(subtotal);

                order.addItem(orderItem);
                totalAmount = totalAmount.add(subtotal);
            }
        }
        
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PLACED);

        Order savedOrder = orderRepository.save(order);

        // Publish to Kafka if delivery is needed
        if (Boolean.TRUE.equals(savedOrder.getDeliveryNeeded())) {
            // Send simple JSON representation or ID. Real implementations would use a specialized Event object.
            kafkaTemplate.send("delivery-requested", "{\"orderId\":" + savedOrder.getOrderId() + "}");
        } else {
            // As per document: No delivery required -> PLACED -> COMPLETED
            savedOrder.setStatus(OrderStatus.COMPLETED);
            savedOrder = orderRepository.save(savedOrder);
        }

        return mapToResponseDTO(savedOrder);
    }

    @Override
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return mapToResponseDTO(order);
    }

    @Override
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDTO> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        // Guard against reprocessing: Document section 7.1 "Order Management checks that an order is not already DELIVERED"
        if (order.getStatus() == OrderStatus.DELIVERED && status == OrderStatus.DELIVERED) {
             return mapToResponseDTO(order); // Ignore duplicate
        }
        
        order.setStatus(status);
        return mapToResponseDTO(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        // Implementation for cancellation logic can be added here
        // Usually requires restoring inventory stock
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        // Restore stock
        for(OrderItem item : order.getItems()) {
            InventoryItem inv = item.getInventoryItem();
            inv.setStockQuantity(inv.getStockQuantity() + item.getQuantity());
            inventoryItemRepository.save(inv);
        }
        order.setStatus(OrderStatus.COMPLETED); // Or CANCELLED if we added it, but spec only has 4 states. Let's use COMPLETED or leave as is if unsupported. 
        orderRepository.save(order);
    }

    private OrderResponseDTO mapToResponseDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems() != null ? order.getItems().stream()
                .map(item -> OrderItemDTO.builder()
                        .itemId(item.getInventoryItem().getItemId())
                        .quantity(item.getQuantity())
                        .unitPriceAtPurchase(item.getUnitPriceAtPurchase())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList()) : Collections.emptyList();

        return OrderResponseDTO.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .deliveryNeeded(order.getDeliveryNeeded())
                .totalAmount(order.getTotalAmount())
                .items(itemDTOs)
                .build();
    }
}
