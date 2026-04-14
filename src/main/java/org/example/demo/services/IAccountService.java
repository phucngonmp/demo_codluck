package org.example.demo.services;

import org.example.demo.dto.AccountDTO;
import org.example.demo.dto.request.RegisterRequest;

public interface IAccountService {
    AccountDTO createAccount(RegisterRequest registerRequest);

    String resolveUsernameByIdentifier(String identifier);
}
