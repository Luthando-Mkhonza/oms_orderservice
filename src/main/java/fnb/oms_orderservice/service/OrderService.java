package fnb.oms_orderservice.service;

import fnb.oms_orderservice.dto.OrderRequestDTO;
import fnb.oms_orderservice.dto.OrderResponseDTO;
import fnb.oms_orderservice.entity.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(OrderRequestDTO requestDTO);
    OrderResponseDTO getOrderById(Long id);
    List<OrderResponseDTO> getAllOrders();
    List<OrderResponseDTO> getOrdersByCustomerId(Long customerId);
    OrderResponseDTO updateOrderStatus(Long id, OrderStatus status);
    void cancelOrder(Long id);
}
