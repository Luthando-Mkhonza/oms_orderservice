package fnb.oms_orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price_at_purchase", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPriceAtPurchase;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;
}
