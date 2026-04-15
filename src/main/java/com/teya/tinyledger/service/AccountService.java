package com.teya.tinyledger.service;

import com.teya.tinyledger.domain.Account;
import com.teya.tinyledger.dto.AccountRequest;
import com.teya.tinyledger.dto.BalanceResponse;
import com.teya.tinyledger.exception.AccountNotFoundException;
import com.teya.tinyledger.repository.AccountRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepo accountRepo;
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    public AccountService(AccountRepo accountRepo) {
        this.accountRepo = accountRepo;
    }

    public Account createAccount(AccountRequest accountRequest) {
        Account account = new Account(accountRequest.getAccountName(), accountRequest.getInitialBalance());
        accountRepo.saveAccount(account.getId(), account);
        return account;
    }

    public BalanceResponse getAccountBalance(UUID accountId) {
        Account account = accountRepo.getAccount(accountId);
        if(account == null) {
            logger.error("Account not found for id: {}", accountId);
            throw new AccountNotFoundException("Account not found for id: " + accountId);
        }
        return new BalanceResponse(account.getId(), account.getName(), account.getBalance());
    }
}

