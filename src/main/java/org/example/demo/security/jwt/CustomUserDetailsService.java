package org.example.demo.security.jwt;

import org.example.demo.common.ErrorCode;
import org.example.demo.entities.Account;
import org.example.demo.exception.AppException;
import org.example.demo.repositories.AccountRepository;
import org.example.demo.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    public CustomUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws AppException {
        Account account = accountRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND));
        return new CustomUserDetails(account);
    }
}

