# Push to GitHub - Setup Guide

The TinyLedger project has been initialized as a local Git repository. Follow these steps to create a GitHub repository and push the code:

## Step 1: Create a GitHub Repository

1. Go to [github.com](https://github.com) and log in to your account
2. Click the **+** icon in the top-right corner and select **New repository**
3. Fill in the repository details:
   - **Repository name**: `TinyLedger`
   - **Description**: A lightweight banking ledger application built with Spring Boot
   - **Public** (to make it a public repo)
   - Do NOT initialize with README, .gitignore, or license (we already have these)
4. Click **Create repository**

## Step 2: Add Remote and Push

After creating the GitHub repository, copy the repository URL and run these commands in the TinyLedger directory:

```bash
# Add the remote repository
git remote add origin https://github.com/YOUR_USERNAME/TinyLedger.git

# Rename branch from main to main (if needed)
git branch -M main

# Push the code to GitHub
git push -u origin main
```

Replace `YOUR_USERNAME` with your actual GitHub username.

## Step 3: Verify

1. Go to your GitHub repository URL: `https://github.com/YOUR_USERNAME/TinyLedger`
2. Verify that all files are there
3. Check the commit history

## Current Git Status

✅ **Local Repository**: Initialized
✅ **Initial Commit**: Created with 27 files
✅ **Commit Hash**: dc30482
✅ **Branch**: main
✅ **Status**: Clean (all files committed)

## Files Included

- **Source Code**: Java classes for controllers, services, repositories, and domain objects
- **Tests**: Integration tests for TransactionService
- **Configuration**: Spring Boot application properties and OpenAPI configuration
- **Build**: Maven pom.xml with all dependencies
- **Documentation**: Comprehensive README.md
- **.gitignore**: Properly configured for Java/Maven projects

## What's Included

### Code Organization
- `src/main/java/com/teya/tinyledger/`: Main application code
- `src/test/java/com/teya/tinyledger/`: Unit and integration tests
- `pom.xml`: Maven build configuration
- `README.md`: Complete project documentation

### Key Features
- Account management with UUID generation
- Transaction management (deposits/withdrawals)
- Concurrent-safe operations with exponential backoff
- Input validation with Spring Bean Validation
- Global exception handling
- OpenAPI 3.0 (Swagger) documentation
- 14 integration tests for TransactionService

### Technologies
- Java 26
- Spring Boot 4.0.5
- Maven 3.8+
- JUnit 5

## Next Steps

1. Push the repository to GitHub (see Step 2 above)
2. Configure branch protection rules (optional)
3. Set up CI/CD with GitHub Actions (optional)
4. Start collaborating!

## Additional Commands

### View commit history
```bash
git log
```

### Check remote configuration
```bash
git remote -v
```

### View branch information
```bash
git branch -a
```

---

**Repository initialized on**: April 15, 2026
**Total commits**: 1
**Total files**: 27

