package com.teya.tinyledger.repository;

import com.teya.tinyledger.domain.Account;

import java.util.function.UnaryOperator;

public interface AccountRepository {

    void createAccount(String accountId, Account account);

    Account getAccount(String accountId);

    Account updateAccount(String accountId, UnaryOperator<Account> updateFunction);
}
