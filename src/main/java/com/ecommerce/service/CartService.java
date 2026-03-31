package com.ecommerce.service;

import com.ecommerce.dto.CartItemRequest;
import com.ecommerce.dto.CartItemResponse;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<CartItemResponse> getCartItems(String username) {
        User user = findUserOrThrow(username);
        return cartItemRepository.findByUser(user).stream()
                .map(CartItemResponse::fromEntity)
                .toList();
    }

    @Transactional
    public CartItemResponse addToCart(String username, CartItemRequest request) {
        User user = findUserOrThrow(username);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + request.getProductId()));

        validateStock(product, request.getQuantity());

        Optional<CartItem> existingItem =
                cartItemRepository.findByUserAndProductId(user, product.getId());

        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            validateStock(product, newQuantity);
            cartItem.setQuantity(newQuantity);
        } else {
            cartItem = CartItem.builder()
                    .user(user)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
        }

        CartItem saved = cartItemRepository.save(cartItem);
        return CartItemResponse.fromEntity(saved);
    }

    @Transactional
    public CartItemResponse updateCartItem(String username, Long productId, Integer quantity) {
        User user = findUserOrThrow(username);
        CartItem cartItem = cartItemRepository.findByUserAndProductId(user, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found for product id: " + productId));

        validateStock(cartItem.getProduct(), quantity);

        cartItem.setQuantity(quantity);
        CartItem updated = cartItemRepository.save(cartItem);
        return CartItemResponse.fromEntity(updated);
    }

    @Transactional
    public void removeFromCart(String username, Long productId) {
        User user = findUserOrThrow(username);
        CartItem cartItem = cartItemRepository.findByUserAndProductId(user, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found for product id: " + productId));
        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCart(String username) {
        User user = findUserOrThrow(username);
        cartItemRepository.deleteByUser(user);
    }

    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with username: " + username));
    }
    
 
    private void validateStock(Product product, int requestedQuantity) {
        if (product.getStockQuantity() < requestedQuantity) {
            throw new BadRequestException(
                    "Not enough stock. Available: " + product.getStockQuantity());
        }
    }
}
