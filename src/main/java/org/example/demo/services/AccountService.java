package org.example.demo.services;

import org.example.demo.common.ErrorCode;
import org.example.demo.common.Role;
import org.example.demo.dto.AccountDTO;
import org.example.demo.dto.request.SignupRequest;
import org.example.demo.entities.Account;
import org.example.demo.exception.AppException;
import org.example.demo.mappers.AccountMapper;
import org.example.demo.repositories.AccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountMapper accountMapper;

    public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountMapper = accountMapper;
    }

    public AccountDTO createAccount(SignupRequest signupRequest) {
        if(!signupRequest.password().equals(signupRequest.confirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_MISMATCH);
        }
        if (isUsernameExists(signupRequest.username())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }
        if(isEmailExists(signupRequest.email())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        Account account = Account.builder()
                .username(signupRequest.username())
                .password(passwordEncoder.encode(signupRequest.password()))
                .email(signupRequest.email())
                .role(Role.USER)
                .build();
        return accountMapper.toDTO(accountRepository.save(account));
    }

    /**
     * Resolve a login identifier (username or email) to username.
     * Throws {@link AppException} if no user exists.
     */
    public String resolveUsernameByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new AppException(ErrorCode.BAD_CREDENTIALS);
        }

        if (isEmail(identifier)) {
            Account account = accountRepository.findByEmail(identifier)
                    .orElseThrow(() -> new AppException(ErrorCode.BAD_CREDENTIALS));
            return account.getUsername();
        }

        // username path (do not leak existence via different error)
        return accountRepository.findByUsername(identifier)
                .map(Account::getUsername)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_CREDENTIALS));
    }

    private boolean isUsernameExists(String username) {
        return accountRepository.existsByUsername(username);
    }
    private boolean isEmailExists(String email) {
        return accountRepository.existsByEmail(email);
    }

    private boolean isEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

}
