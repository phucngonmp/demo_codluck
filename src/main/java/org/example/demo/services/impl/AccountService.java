package org.example.demo.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.demo.common.ErrorCode;
import org.example.demo.dto.AccountDTO;
import org.example.demo.dto.request.RegisterRequest;
import org.example.demo.entities.Account;
import org.example.demo.entities.Role;
import org.example.demo.exception.ClientException;
import org.example.demo.mappers.AccountMapper;
import org.example.demo.repositories.AccountRepository;
import org.example.demo.repositories.RoleRepository;
import org.example.demo.services.IAccountService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AccountService implements IAccountService {
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountMapper accountMapper;

    public AccountService(AccountRepository accountRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountMapper = accountMapper;
    }

    @Override
    public AccountDTO createAccount(RegisterRequest registerRequest) {
        if(!registerRequest.password().equals(registerRequest.confirmPassword())) {
            throw new ClientException(ErrorCode.PASSWORD_MISMATCH);
        }
        if (isUsernameExists(registerRequest.username())) {
            throw new ClientException(ErrorCode.USERNAME_EXISTED);
        }
        if(isEmailExists(registerRequest.email())) {
            throw new ClientException(ErrorCode.EMAIL_EXISTED);
        }
        Account account = Account.builder()
                .username(registerRequest.username())
                .password(passwordEncoder.encode(registerRequest.password()))
                .email(registerRequest.email())
                .roles(List.of(getOrCreateRole("USER")))
                .build();
        return accountMapper.toDTO(accountRepository.save(account));
    }

    @Override
    public String resolveUsernameByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new ClientException(ErrorCode.BAD_CREDENTIALS);
        }
        if (isEmail(identifier)) {
            Account account = accountRepository.findByEmail(identifier)
                    .orElseThrow(() -> new ClientException(ErrorCode.BAD_CREDENTIALS));
            return account.getUsername();
        }
        return accountRepository.findByUsername(identifier)
                .map(Account::getUsername)
                .orElseThrow(() -> new ClientException(ErrorCode.BAD_CREDENTIALS));
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

    private Role getOrCreateRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));
    }

}
