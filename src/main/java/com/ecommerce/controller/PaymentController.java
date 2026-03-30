package com.ecommerce.controller;

import com.ecommerce.dto.PaymentResponse;
import com.ecommerce.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-intent/{orderId}")
    public ResponseEntity<PaymentResponse> createPaymentIntent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        PaymentResponse response = paymentService.createPaymentIntent(
                userDetails.getUsername(), orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody Map<String, Object> payload) {
        
        String type = (String) payload.get("type");

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        @SuppressWarnings("unchecked")
        Map<String, Object> object = (Map<String, Object>) data.get("object");
        String paymentIntentId = (String) object.get("id");

        switch (type) {
            case "payment_intent.succeeded":
                paymentService.handlePaymentSuccess(paymentIntentId);
                break;
            case "payment_intent.payment_failed":
                paymentService.handlePaymentFailure(paymentIntentId);
                break;
            default:
               
                break;
        }

        return ResponseEntity.ok("Webhook processed");
    }
}
