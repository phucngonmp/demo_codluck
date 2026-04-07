package org.example.demo.service;

import org.example.demo.authentication.*;

public interface IAuthService {
    AuthenticationResponse authenticate (AuthenticationRequest authenticationRequest);

    RegisterResponse register(RegisterRequest registerRequest);

    String sendOtp(String email);

    String requestPasswordReset(String email);

    Boolean verifyOtp(String email, String code);

    String changePassword(RequestPasswordReset requestPasswordReset);
}
