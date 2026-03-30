package com.ecommerce.service;

import com.ecommerce.dto.OrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.entity.*;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        CartItemRepository cartItemRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponse createOrderFromCart(String username, OrderRequest request) {
        User user = findUserOrThrow(username);
        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty. Add items before placing an order.");
        }

        Order order = Order.builder()
                .user(user)
                .shippingAddress(request.getShippingAddress())
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException(
                        "Insufficient stock for product: " + product.getName()
                        + ". Available: " + product.getStockQuantity());
            }

            // Reduce stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();

            order.addOrderItem(orderItem);
            totalPrice = totalPrice.add(
                    product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setTotalPrice(totalPrice);
        Order savedOrder = orderRepository.save(order);

        // Clear the cart after successful order creation
        cartItemRepository.deleteByUser(user);

        return OrderResponse.fromEntity(savedOrder);
    }

    public List<OrderResponse> getUserOrders(String username) {
        User user = findUserOrThrow(username);
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    public OrderResponse getOrderById(String username, Long orderId) {
        User user = findUserOrThrow(username);
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId));
        return OrderResponse.fromEntity(order);
    }

    @Transactional
    public void updatePaymentStatus(String paymentIntentId, PaymentStatus status) {
        Order order = orderRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found for payment intent: " + paymentIntentId));
        order.setPaymentStatus(status);
        orderRepository.save(order);
    }

    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with username: " + username));
    }
    
    @Transactional
    public OrderResponse updateOrderStatusTest(String username, Long orderId, PaymentStatus status) {
        User user = findUserOrThrow(username);
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId));
        order.setPaymentStatus(status);
        orderRepository.save(order);
        return OrderResponse.fromEntity(order);
    }
}
