package com.teya.tinyledger.service;

import com.teya.tinyledger.domain.Account;
import com.teya.tinyledger.dto.AccountRequest;
import com.teya.tinyledger.dto.AccountResponse;
import com.teya.tinyledger.exception.AccountNotFoundException;
import com.teya.tinyledger.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private AccountRequest accountRequest;

    @BeforeEach
    void setup() {
        accountRequest = new AccountRequest("Test Account", new BigDecimal(1000));
    }

    @Test
    void shouldCreateAccountSuccessfully() {
        Account result = accountService.createAccount(accountRequest);

        assertNotNull(result);
        assertEquals("Test Account", result.getName());
        assertEquals(new BigDecimal(1000), result.getBalance());

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);

        verify(accountRepository).createAccount(idCaptor.capture(), accountCaptor.capture());

        assertEquals(result.getId(), idCaptor.getValue());
        assertEquals(result, accountCaptor.getValue());
    }

    @Test
    void shouldReturnAccountWhenExists() {
        Account account = new Account("Test Account", new BigDecimal(500));
        String accountId = account.getId();

        when(accountRepository.getAccount(accountId)).thenReturn(account);

        Account result = accountService.validateAccountExists(accountId);

        assertNotNull(result);
        assertEquals(account, result);
    }

    @Test
    void shouldThrowExceptionWhenAccountDoesNotExist() {
        String accountId = "non-existent-id";
        when(accountRepository.getAccount(accountId)).thenReturn(null);

        AccountNotFoundException exception = assertThrows(
                AccountNotFoundException.class,
                () -> accountService.validateAccountExists(accountId)
        );

        assertEquals("Account not found for id: " + accountId, exception.getMessage());
        verify(accountRepository).getAccount(accountId);
    }

    @Test
    void shouldReturnAccountResponse() {
        Account account = new Account("Test Account", new BigDecimal(750));
        String accountId = account.getId();

        when(accountRepository.getAccount(accountId)).thenReturn(account);

        AccountResponse response = accountService.getAccount(accountId);

        assertNotNull(response);
        assertEquals(accountId, response.getAccountId());
        assertEquals("Test Account", response.getAccountName());
        assertEquals(new BigDecimal(750), response.getBalance());
    }
}