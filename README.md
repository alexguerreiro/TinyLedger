# TinyLedger

A lightweight banking ledger application built with Spring Boot for managing accounts and transactions.

## Features

- **Account Management**: Create and manage bank accounts with UUIDs
- **Transactions**: Perform deposits and withdrawals with proper validation
- **Transaction History**: View complete transaction history sorted by timestamp
- **Concurrency Control**: Thread-safe operations with exponential backoff retry mechanism
- **Input Validation**: Comprehensive validation using Spring Bean Validation
- **Error Handling**: Centralized error handling with proper HTTP status codes
- **API Documentation**: OpenAPI 3.0 (Swagger) documentation with interactive UI
- **Unit Tests**: Realistic integration tests using real repository instances

## Technologies

- **Java**: 26 (Latest)
- **Spring Boot**: 4.0.5
- **Spring Web**: REST API development
- **Spring Validation**: Input validation
- **SpringDoc OpenAPI**: 2.5.0 (Swagger/OpenAPI 3.0)
- **JUnit 5**: Unit testing
- **Maven**: Build tool

## Getting Started

### Prerequisites

- Java 26 or higher
- Maven 3.8.0 or higher
- Git

### Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/TinyLedger.git
cd TinyLedger
```

2. Build the project:
```bash
./mvnw clean install
```

3. Run the application:
```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## API Documentation

### Swagger UI
Interactive API documentation is available at:
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON
The OpenAPI specification is available at:
```
http://localhost:8080/v3/api-docs
```

## API Endpoints

### Accounts

- **Create Account**
  ```
  POST /api/v1/accounts
  ```
  Create a new account with initial balance

- **Get Account Balance**
  ```
  GET /api/v1/accounts/{accountId}/balance
  ```
  Retrieve current balance for an account

### Transactions

- **Deposit**
  ```
  POST /api/v1/accounts/transactions/deposit
  ```
  Add funds to an account

- **Withdrawal**
  ```
  POST /api/v1/accounts/transactions/withdrawal
  ```
  Remove funds from an account

- **Transaction History**
  ```
  GET /api/v1/accounts/{accountId}/transactions
  ```
  View complete transaction history

## Example Requests

### Create Account
```bash
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "accountName": "John Doe",
    "initialBalance": 1000.0
  }'
```

### Deposit Money
```bash
curl -X POST http://localhost:8080/api/v1/accounts/transactions/deposit \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "550e8400-e29b-41d4-a716-446655440000",
    "amount": 500.0
  }'
```

### Withdraw Money
```bash
curl -X POST http://localhost:8080/api/v1/accounts/transactions/withdrawal \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "550e8400-e29b-41d4-a716-446655440000",
    "amount": 250.0
  }'
```

### Get Transaction History
```bash
curl -X GET http://localhost:8080/api/v1/accounts/550e8400-e29b-41d4-a716-446655440000/transactions
```

## Testing

Run the test suite:
```bash
./mvnw test
```

Run a specific test:
```bash
./mvnw test -Dtest=TransactionServiceTest
```

## Project Structure

```
src/
├── main/
│   ├── java/com/teya/tinyledger/
│   │   ├── TinyLedgerApplication.java          # Main application
│   │   ├── config/
│   │   │   └── OpenAPIConfig.java              # OpenAPI configuration
│   │   ├── controller/
│   │   │   ├── AccountController.java          # Account endpoints
│   │   │   └── TransactionController.java      # Transaction endpoints
│   │   ├── domain/
│   │   │   ├── Account.java                    # Account entity
│   │   │   ├── Transaction.java                # Transaction record
│   │   │   └── OperationType.java              # Enum for operation types
│   │   ├── dto/
│   │   │   ├── AccountRequest.java
│   │   │   ├── AccountResponse.java
│   │   │   ├── TransactionRequest.java
│   │   │   ├── TransactionResponse.java
│   │   │   └── BalanceResponse.java
│   │   ├── exception/
│   │   │   └── GlobalExceptionHandler.java     # Global exception handling
│   │   ├── repository/
│   │   │   └── AccountRepo.java                # Account repository
│   │   └── service/
│   │       ├── AccountService.java             # Account business logic
│   │       └── TransactionService.java         # Transaction business logic
│   └── resources/
│       └── application.properties              # Configuration
└── test/
    └── java/com/teya/tinyledger/
        └── service/
            └── TransactionServiceTest.java     # Integration tests
```

## Key Features

### Concurrency Control
- **Atomic Updates**: Compare-and-swap (CAS) approach for thread-safe operations
- **Exponential Backoff**: Retry mechanism with exponential delays to handle contention
- **ConcurrentHashMap**: Thread-safe data structure for account storage

### Input Validation
- `@NotNull` - Required fields
- `@Positive` - Amount must be positive
- `@NotBlank` - Account name cannot be empty
- Custom validation in GlobalExceptionHandler

### Error Handling
- Centralized exception handling with `@RestControllerAdvice`
- Proper HTTP status codes (201, 400, 404, 500)
- Detailed error messages with validation details

### API Design
- RESTful endpoints with `/api/v1` versioning
- Clear separation of concerns (Controller, Service, Repository)
- Immutable domain objects using Java records
- Comprehensive OpenAPI documentation

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Author

TinyLedger Team

## Support

For support, please open an issue in the GitHub repository.

---

**Last Updated**: April 15, 2026

