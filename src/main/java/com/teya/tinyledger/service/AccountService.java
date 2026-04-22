package com.teya.tinyledger.service;

import com.teya.tinyledger.domain.Account;
import com.teya.tinyledger.dto.AccountRequest;
import com.teya.tinyledger.dto.BalanceResponse;
import com.teya.tinyledger.exception.AccountNotFoundException;
import com.teya.tinyledger.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(AccountRequest accountRequest) {
        Account account = new Account(accountRequest.getAccountName(), accountRequest.getInitialBalance());
        accountRepository.createAccount(account.getId(), account);
        return account;
    }

    public BalanceResponse getAccountBalance(String accountId) {
        Account account = accountRepository.getAccount(accountId);
        if(account == null) {
            logger.error("Account not found for id: {}", accountId);
            throw new AccountNotFoundException("Account not found for id: " + accountId);
        }
        return new BalanceResponse(account.getId(), account.getName(), account.getBalance());
    }
}

