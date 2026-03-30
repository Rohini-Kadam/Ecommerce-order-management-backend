package com.ecommerce.service;

import com.ecommerce.dto.PaymentResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.PaymentStatus;
import com.ecommerce.exception.PaymentException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    @Value("${stripe.api.secret-key}")
    private String stripeSecretKey;

    private final OrderRepository orderRepository;

    public PaymentService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Transactional
    public PaymentResponse createPaymentIntent(String username, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId));

        // Verify the order belongs to the requesting user
        if (!order.getUser().getUsername().equals(username)) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }

        if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new PaymentException("This order has already been paid");
        }

        try {
            // Stripe expects amount in the smallest currency unit (cents)
            long amountInCents = order.getTotalPrice()
                    .multiply(java.math.BigDecimal.valueOf(100))
                    .longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("usd")
                    .putMetadata("order_id", orderId.toString())
                    .putMetadata("username", username)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Save the payment intent ID on the order
            order.setStripePaymentIntentId(paymentIntent.getId());
            orderRepository.save(order);

            return new PaymentResponse(
                    paymentIntent.getClientSecret(),
                    paymentIntent.getId(),
                    paymentIntent.getStatus());

        } catch (StripeException e) {
            throw new PaymentException("Payment processing failed: " + e.getMessage());
        }
    }

    @Transactional
    public void handlePaymentSuccess(String paymentIntentId) {
        Order order = orderRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found for payment intent: " + paymentIntentId));
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        orderRepository.save(order);
    }

    @Transactional
    public void handlePaymentFailure(String paymentIntentId) {
        Order order = orderRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found for payment intent: " + paymentIntentId));
        order.setPaymentStatus(PaymentStatus.FAILED);
        orderRepository.save(order);
    }
}
