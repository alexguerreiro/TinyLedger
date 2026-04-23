package com.teya.tinyledger.controller;

import com.teya.tinyledger.domain.Account;
import com.teya.tinyledger.dto.AccountRequest;
import com.teya.tinyledger.dto.AccountResponse;
import com.teya.tinyledger.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts", description = "APIs for managing ledger accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @Operation(
            summary = "Create a new account",
            description = "Creates a new account with the specified name and initial balance.",
            tags = {"Accounts"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Account created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - account name is required, balance cannot be negative"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountRequest accountRequest) {
        Account account = accountService.createAccount(accountRequest);
        AccountResponse response = new AccountResponse(account.getId(), account.getName(), account.getBalance());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{accountId}")
    @Operation(
            summary = "Get an account",
            description = "Retrieves the current representation of the specified account.",
            tags = {"Accounts"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Account retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponse.class))
                ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public ResponseEntity<AccountResponse> getAccount(
            @Parameter(description = "The unique identifier of the account", required = true)
            @PathVariable String accountId) {
        AccountResponse account = accountService.getAccount(accountId);
        return ResponseEntity.ok(account);
    }
}
