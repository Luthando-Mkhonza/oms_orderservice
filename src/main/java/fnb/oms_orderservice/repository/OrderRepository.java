package fnb.oms_orderservice.repository;

import fnb.oms_orderservice.entity.Order;
import fnb.oms_orderservice.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Custom query method to find orders by a specific customer
    List<Order> findByCustomerId(Long customerId);
    
    // Custom query method to find orders by their current status
    List<Order> findByStatus(OrderStatus status);
}
