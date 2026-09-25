package fnb.oms_orderservice.dto;

import fnb.oms_orderservice.entity.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponseDTO {
    private Long orderId;
    private Long customerId;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private Boolean deliveryNeeded;
    private BigDecimal totalAmount;
    private List<OrderItemDTO> items;
}
