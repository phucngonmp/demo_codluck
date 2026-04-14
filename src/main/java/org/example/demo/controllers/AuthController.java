package org.example.demo.controllers;

import jakarta.validation.Valid;
import org.example.demo.common.ApiResponse;
import org.example.demo.common.ErrorCode;
import org.example.demo.dto.AccountDTO;
import org.example.demo.dto.request.LoginRequest;
import org.example.demo.dto.request.RegisterRequest;
import org.example.demo.dto.response.AuthResponse;
import org.example.demo.entities.Role;
import org.example.demo.exception.ClientException;
import org.example.demo.security.CustomUserDetails;
import org.example.demo.security.jwt.JwtUtil;
import org.example.demo.services.IAccountService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final IAccountService accountService;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    AuthController(IAccountService accountService,
                   JwtUtil jwtUtil,
                   UserDetailsService userDetailsService,
                   AuthenticationManager authenticationManager) {
        this.accountService = accountService;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
    }


    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        String username = accountService.resolveUsernameByIdentifier(loginRequest.identifier());

        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, loginRequest.password())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String accessToken = jwtUtil.generateAccessToken(userDetails);

            AccountDTO accountDTO = new AccountDTO(
                    userDetails.getUsername(),
                    userDetails.getAccount().getEmail(),
                    userDetails.getAccount().getRoles()
                            .stream()
                            .map(Role::getName)
                            .collect(Collectors.toList())
            );

            return ApiResponse.success(new AuthResponse(accessToken, accountDTO));
        } catch (BadCredentialsException ex) {
            throw new ClientException(ErrorCode.BAD_CREDENTIALS);
        }
    }
    @PostMapping("/register")
    public ApiResponse<AuthResponse> signup(@RequestBody @Valid RegisterRequest registerRequest) {
        AccountDTO accountDTO = accountService.createAccount(registerRequest);
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(accountDTO.username());
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        return ApiResponse.success(new AuthResponse(accessToken, accountDTO));
    }

}
