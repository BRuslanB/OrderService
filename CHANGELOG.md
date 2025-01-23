# Changelog

## [v1.0.0] - 2025-01-14
### Added
- CRUD operations for Order and Product.
- Endpoints for working with the service.
- Tests for the core components of the service.
- Logging functionality using `logback.xml`.

### Fixed
- No known issues.

## [v1.1.0] - 2025-01-19
### Added
- Integrated Spring Security.
- Implemented authentication functionality with JWT tokens.
- Added `data.sql` and `schema.sql` scripts for creating test tables and data.
- Added and updated tests for the core components of the service.
- Enabled support for Docker containers with Redis and PostgreSQL.

### Fixed
- No known issues.

## [v1.2.0] - 2025-01-23
### Added
- Added Redis for caching data related to orders and products.
- Implemented invalid token storage (blacklist) in Redis.
- Centralized logging of user activities.
- Added support for Springdoc (`/api-docs`, `/swagger-ui`).
- Added support for Actuator (`/info`, `/healthcheck`, `/metrics`).
- Added custom metrics for counting successful and failed orders.
- Added and updated tests for the core components of the service.

### Fixed
- No known issues.

## Git Flow History
1. The `feature/add-order-product-crud` branch was completed and merged into `develop`.
2. The `feature/add-application-files` branch was completed and merged into `develop`.
3. The `feature/add-tests` branch was completed and merged into `develop`.
4. Changes were released from `develop` to `main` with tag `v1.0.0`.
5. The `feature/update-order-product-crud` branch was completed and merged into `develop`.
6. The `feature/update-application-files` branch was completed and merged into `develop`.
7. The `feature/update-tests` branch was completed and merged into `develop`.
8. Changes were released from `develop` to `main` with tag `v1.1.0`.
9. The `feature/update-core-v1.2.0` branch was completed and merged into `develop`.
10. The `feature/update-config-v1.2.0` branch was completed and merged into `develop`.
11. The `feature/update-tests-v1.2.0` branch was completed and merged into `develop`.
12. Changes were released from `develop` to `main` with tag `v1.2.0`.
