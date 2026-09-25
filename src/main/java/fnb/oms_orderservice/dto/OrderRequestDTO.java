package fnb.oms_orderservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequestDTO {
    private Long customerId;
    private Boolean deliveryNeeded;
    private List<OrderItemDTO> items;
}
