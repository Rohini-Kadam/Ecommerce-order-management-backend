## Ecommerce Order Management Backend
A REST API backend for an e-commerce platform built with Spring Boot. Handles user authentication, product management, shopping cart, order processing, and Stripe payment integration.

## Tech Stack:
Java 17
Spring Boot 3
Spring Security
JWT (JSON Web Tokens)
MySQL 8.0
Hibernate / JPA
Stripe API
Maven

## Setup

1. Create the database
sqlCREATE DATABASE ecommerce;
2. Set environment variables
Open Eclipse and go to Run → Run Configurations → Environment tab and add the following:

DB_USERNAME        your MySQL username
DB_PASSWORD        your MySQL password
JWT_SECRET         any long random string
JWT_EXPIRATION     86400000
API_SECRET_KEY     your Stripe secret key (sk_test_...)

3. Run the application
Right click the project in Eclipse → Run As → Spring Boot App
The app starts on http://localhost:8080
Tables and sample data are created automatically on first run.

## API Endpoints

1. Authentication
POST  /api/auth/register     Register a new user
POST  /api/auth/login        Login and receive JWT token

3. Products
GET     /api/products           Get all products (public)
GET     /api/products/{id}      Get single product (public)
POST    /api/products           Add product (admin only)
PUT     /api/products/{id}      Update product (admin only)
DELETE  /api/products/{id}      Delete product (admin only)

3.Cart
GET     /api/cart                        View cart items
POST    /api/cart                        Add item to cart
PUT     /api/cart/{productId}?quantity=2 Update item quantity
DELETE  /api/cart/{productId}            Remove item from cart
DELETE  /api/cart                        Clear entire cart

4.Orders

POST  /api/orders       Place an order from cart
GET   /api/orders       Get all orders
GET   /api/orders/{id}  Get single order

5. Payments

POST  /api/payments/create-intent/{orderId}   Create Stripe payment intent
POST  /api/payments/webhook                   Stripe webhook handler

## All protected routes require this header:
Authorization: Bearer your_token_here

