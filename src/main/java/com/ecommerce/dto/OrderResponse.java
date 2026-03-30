package com.ecommerce.dto;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private BigDecimal totalPrice;
    private String shippingAddress;
    private String paymentStatus;
    private String stripePaymentIntentId;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;

    public static OrderResponse fromEntity(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(OrderItemResponse::fromEntity)
                .toList();

        return new OrderResponse(
            order.getId(),
            order.getTotalPrice(),
            order.getShippingAddress(),
            order.getPaymentStatus().name(),
            order.getStripePaymentIntentId(),
            order.getCreatedAt(),
            items
        );
    }

    @Data
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;

        public static OrderItemResponse fromEntity(OrderItem item) {
            return new OrderItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice()
            );
        }
    }
}
