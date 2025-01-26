# Order Service

This Java-based backend service provides a REST API for managing customer orders and products, including CRUD operations and real-time order status updates.

> **Note:** This project was developed as a test task to demonstrate expertise in Java development and microservices using the Spring Framework.

---

## Features

### Order Management
- Create, update, delete, and retrieve orders.
- Automatically calculate the total price of an order.
- Handle order status updates with event generation.

### Product Management
- Manage products associated with orders.
- Validate product details during order operations.

### Additional Features
- Soft delete functionality for orders.
- Centralized error handling for all API endpoints.
- Data caching using Redis.
- User activity logging.
- Custom metrics to monitor successful and failed order operations.

---

## Technology Stack

- **Java 21**
- **Spring Boot** (REST API, Security, Validation, AOP, Actuator)
- **Spring Data JPA** (Hibernate for ORM)
- **PostgreSQL** (Relational Database)
- **Redis** (Caching)
- **Docker** (Containerization)
- **H2** (In-memory database for tests)
- **Gradle** (Build automation tool)
- **Lombok** (Reduces boilerplate code)
- **Springdoc OpenAPI** (Swagger UI for API documentation)
- **Logback** (Logging)
- **Git Flow** (Branching model for version control)

---

## Getting Started

To run the Order Service locally, follow these steps:

1. Clone the repository to your local machine:
   ```bash
   git clone https://github.com/BRuslanB/OrderService.git
   ```
2. Start the service using Docker Compose:
   ```bash
   docker-compose up -d
   ```

### Running Tests

Execute the test suite using Gradle:

```bash
./gradlew test
```

## Access the Service

### API Documentation:
- **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

### Healthcheck:
- [http://localhost:8080/healthcheck](http://localhost:8080/healthcheck)

### Metrics:
- [http://localhost:8080/metrics](http://localhost:8080/metrics)

---

## Metrics and Monitoring

**Spring Boot Actuator** is used for monitoring the system and exposing metrics.

### Available Endpoints:
- **Application Info:** `/info`
- **Health Check:** `/healthcheck`
- **Metrics:** `/metrics`

### Custom Metrics:
- **`custom.successful.orders`** — Number of successfully processed orders.
- **`custom.failed.orders`** — Number of failed order operations.

---

## Contributing

Contributions are welcome! If you would like to contribute to this project, please follow these steps:

1. Use **Git Flow** branching model for feature development.
2. Create feature branches from `develop` (e.g., `feature/add-new-endpoint`).
3. Ensure all changes are covered with tests.
4. Submit a pull request with a clear description of your changes.

For more details on **Git Flow**, refer to the [Git Flow Documentation](https://nvie.com/posts/a-successful-git-branching-model/).

## License

This project is licensed under the MIT License.

---

Feel free to open issues or submit pull requests to improve the service.
   