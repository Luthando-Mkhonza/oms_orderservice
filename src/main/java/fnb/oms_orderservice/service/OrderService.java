package fnb.oms_orderservice.service;

import fnb.oms_orderservice.entity.Order;
import fnb.oms_orderservice.entity.OrderStatus;

import java.util.List;

public interface OrderService {
    Order createOrder(Order order);
    Order getOrderById(Long id);
    List<Order> getAllOrders();
    List<Order> getOrdersByCustomerId(Long customerId);
    Order updateOrderStatus(Long id, OrderStatus status);
    void cancelOrder(Long id);
}
