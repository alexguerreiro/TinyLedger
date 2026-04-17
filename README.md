# TinyLedger

A lightweight banking ledger application built with Spring Boot for managing accounts and transactions.

## Features

- **Account Management**: Create and manage bank accounts
- **Transactions**: Perform deposits and withdrawals with proper validation
- **Transaction History**: View complete transaction history sorted by timestamp
- **Input Validation**: Comprehensive validation using Spring Bean Validation
- **Error Handling**: Centralized error handling with proper HTTP status codes
- **Retry logic**: Retry failed transactions in separated job
- **API Documentation**: OpenAPI 3.0 (Swagger) documentation with interactive UI
- **Unit Tests**: Realistic integration tests using real repository (in-memory map)

## Technologies

- **Java**: 26 (Latest)
- **Spring Boot**: 4.0.5
- **Spring Web**: REST API development
- **Spring Validation**: Input validation
- **SpringDoc OpenAPI**: 3.0.1 (Swagger/OpenAPI 3.0)
- **JUnit 5**: Unit testing
- **Maven**: Build tool

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

## Testing

Run the test suite:
```bash
./mvnw test
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
│   │   │   ├── FailedTransaction.java          # Failed transaction record for retry queue
│   │   │   └── TransactionType.java            # Enum for operation types
│   │   ├── dto/
│   │   │   ├── AccountRequest.java
│   │   │   ├── AccountResponse.java
│   │   │   ├── TransactionRequest.java
│   │   │   ├── TransactionResponse.java
│   │   │   ├── TransactionHistoryResponse.java
│   │   │   └── BalanceResponse.java
│   │   ├── exception/
│   │   │   ├── AccountNotFoundException.java
│   │   │   ├── DatabaseUpdateException.java    # Custom exception for database update failures (trggered for retryable errors)
│   │   │   └── GlobalExceptionHandler.java     # Global exception handling
│   │   ├── queue/
│   │   │   └── TransactionQueue.java           # Queue for retrying failed transactions
│   │   ├── repository/
│   │   │   └── AccountRepo.java                # Account repository
│   │   └── service/
│   │       ├── AccountService.java             # Account business logic
│   │       ├── TransactionRetryScheduler.java  # Scheduler for retrying failed transactions
│   │       └── TransactionService.java         # Transaction business logic
│   └── resources/
│       └── application.properties              # Configuration
└── test/
    └── java/com/teya/tinyledger/
        └── service/
            └── TransactionServiceTest.java     # Integration tests
```

