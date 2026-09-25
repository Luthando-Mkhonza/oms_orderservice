package fnb.oms_orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long itemId;
    private Integer quantity;
    private BigDecimal unitPriceAtPurchase;
    private BigDecimal subtotal;
}
