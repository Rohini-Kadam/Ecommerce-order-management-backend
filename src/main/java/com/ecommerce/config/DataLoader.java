package com.ecommerce.config;

import com.ecommerce.entity.Product;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("!test")
public class DataLoader implements CommandLineRunner {

	@Value("${app.admin.username:rohi}")
    private String adminUsername;

    @Value("${app.admin.email:rohi@gmail.com}")
    private String adminEmail;

    @Value("${app.admin.password:rohi$555}")
    private String adminPassword;

    @Value("${app.user.username:shruti}")
    private String userUsername;

    @Value("${app.user.email:shruti@gmail.com}")
    private String userEmail;

    @Value("${app.user.password:shruti$333}")
    private String userPassword;
    
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository,
                      ProductRepository productRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            //Admin user
            User admin = User.builder()
            		.username(adminUsername)       
                    .email(adminEmail)             
                    .password(passwordEncoder.encode(adminPassword)) 
                    .role(Role.ROLE_ADMIN)
                    .build();
            userRepository.save(admin);

            // Regular user
            User user = User.builder()
            		.username(userUsername)        
                    .email(userEmail)              
                    .password(passwordEncoder.encode(userPassword)) 
                    .role(Role.ROLE_USER)
                    .build();
            userRepository.save(user);
        }

        if (productRepository.count() == 0) {
            productRepository.save(Product.builder()
                    .name("Wireless Bluetooth Headphones")
                    .description("Premium noise-cancelling headphones with 30-hour battery life")
                    .price(new BigDecimal("700.50"))
                    .stockQuantity(150)
                    .imageUrl("https://drive.google.com/file/d/17f2h7F8isqMqBOMkbLR4vRAjE2YK2sN9/view?usp=sharing")
                    .build());

            productRepository.save(Product.builder()
                    .name("USB-C Charging Cable")
                    .description("6ft braided nylon USB-C fast charging cable")
                    .price(new BigDecimal("400.99"))
                    .stockQuantity(500)
                    .imageUrl("https://drive.google.com/file/d/1C5lmC7Rh9oPPymq864H-_lpQKeGd-jAp/view?usp=sharing")
                    .build());

            productRepository.save(Product.builder()
                    .name("Laptop Stand")
                    .description("Adjustable aluminum laptop stand for ergonomic viewing")
                    .price(new BigDecimal("1200.00"))
                    .stockQuantity(80)
                    .imageUrl("https://drive.google.com/file/d/1GUA5JiR2x2BdXNd_bQ2UUAXlBWEVVHuN/view?usp=sharing")
                    .build());

            productRepository.save(Product.builder()
                    .name("Mechanical Keyboard")
                    .description("RGB mechanical gaming keyboard with Cherry MX switches")
                    .price(new BigDecimal("1100.99"))
                    .stockQuantity(60)
                    .imageUrl("https://drive.google.com/file/d/1_lJCfIR8m7CAUOOfWDcqUdTYbhJp4X_I/view?usp=sharing")
                    .build());
        }
    }
}
