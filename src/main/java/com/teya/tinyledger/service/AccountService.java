package com.teya.tinyledger.service;

import com.teya.tinyledger.domain.Account;
import com.teya.tinyledger.dto.AccountRequest;
import com.teya.tinyledger.dto.BalanceResponse;
import com.teya.tinyledger.repository.AccountRepo;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepo accountRepo;

    public AccountService(AccountRepo accountRepo) {
        this.accountRepo = accountRepo;
    }

    public Account createAccount(AccountRequest accountRequest) {
        Account account = new Account(accountRequest.getAccountName(), accountRequest.getInitialBalance());
        accountRepo.saveAccount(account.getId(), account);
        return account;
    }

    public Account getAccount(UUID accountId) {
        Account account = accountRepo.getAccount(accountId);
        if(account == null) {
            throw new IllegalStateException("Account not found for id: " + accountId);
        }
        return account;
    }

    public BalanceResponse getAccountBalance(UUID accountId) {
        Account account = accountRepo.getAccount(accountId);
        if(account == null) {
            throw new IllegalStateException("Account not found for id: " + accountId);
        }
        return new BalanceResponse(account.getId(), account.getName(), account.getBalance());
    }
}

