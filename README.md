# Order Service

This backend Java-based service provides a REST API for managing customer orders and products, enabling CRUD operations and real-time order status updates.

Note: This project was created as a test task to demonstrate expertise in Java development and Spring framework-based microservices. 

## Features

The Order Service includes the following features:

### Order Management
- Create, update, delete, and fetch orders.
- Automatically calculate the total price of an order.
- Handle order status updates with event generation for further integration.

### Product Management
- Manage products associated with orders.
- Validate product details during order operations.

### Additional Capabilities
- Soft delete functionality for orders.
- Centralized error handling and validation for all API endpoints.

## Technologies Used

The Order Service is built with the following stack:
- **Java 21**
- **Spring Boot** (REST API, Validation, AOP)
- **Spring Data JPA** (Hibernate)
- **PostgreSQL** (Relational Database)
- **H2** (In-memory Database for Tests)
- **JUnit** (Test Framework)
- **Lombok** (Simplifies Java Code)
- **Docker** (Containerization)
- **Gradle** (Build Automation Tool)
- **Logback** (Logging)
- **Git Flow** (Branching Workflow)

## Prerequisites

Before running the service, ensure the following software is installed on your system:
- **Java Development Kit (JDK)** 21 or higher
- **Docker**

## Getting Started

To run the Order Service locally, follow these steps:

1. Clone the repository to your local machine:
   ```bash
   git clone https://github.com/BRuslanB/OrderService.git

2. Navigate to the project directory:
   ```bash
   cd order-service
   ```

3. Build the project:
   ```bash
   ./gradlew build
   ```

4. Start the service using Docker Compose:
   ```bash
   docker-compose up -d
   ```

5. Access the API documentation:
   - Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### Running Tests

To execute the test suite:

```bash
./gradlew test
```

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
