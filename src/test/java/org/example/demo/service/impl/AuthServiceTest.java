package org.example.demo.service.impl;

import org.example.demo.authentication.RegisterRequest;
import org.example.demo.authentication.RegisterResponse;
import org.example.demo.entities.Otp;
import org.example.demo.entities.Role;
import org.example.demo.entities.User;
import org.example.demo.exception.MessageError;
import org.example.demo.repositories.OtpRepository;
import org.example.demo.repositories.RoleRepository;
import org.example.demo.repositories.UserRepository;
import org.example.demo.security.UserDetailService;
import org.example.demo.service.II18nService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetailService userDetailService;

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EmailService emailService;
    @Mock
    private II18nService i18nService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerShouldCreateUnverifiedUserAndSendOtp() {
        RegisterRequest request = new RegisterRequest("new-user@example.com", "Password1!");
        Role role = new Role();
        role.setName("USER");
        when(emailService.generateOTP()).thenReturn("123456");
        when(roleRepository.findByName("USER")).thenReturn(role);

        RegisterResponse response = authService.register(request);

        assertEquals("new-user@example.com", response.getEmail());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("new-user@example.com", savedUser.getEmail());
        assertFalse(savedUser.isVerified());
        assertTrue(savedUser.getPassword().startsWith("$2a$") || savedUser.getPassword().startsWith("$2b$"));

        verify(otpRepository).deleteByEmail("new-user@example.com");

        ArgumentCaptor<Otp> otpCaptor = ArgumentCaptor.forClass(Otp.class);
        verify(otpRepository).save(otpCaptor.capture());
        verify(emailService).sendOTPEmail("new-user@example.com", "123456");

        Otp savedOtp = otpCaptor.getValue();
        assertEquals("new-user@example.com", savedOtp.getEmail());
        assertEquals("123456", savedOtp.getOtpCode());
        assertNotNull(savedOtp.getExpiresAt());
    }

    @Test
    void sendOtpShouldReturnSafeMessageAndPersistFreshOtpForUnverifiedAccount() {
        User user = new User();
        user.setEmail("new-user@example.com");
        user.setVerified(false);
        when(userRepository.findByEmail("new-user@example.com")).thenReturn(user);
        when(emailService.generateOTP()).thenReturn("123456");
        when(i18nService.getMessage("auth.success.otpSent")).thenReturn("OTP sent to your email.");

        String response = authService.sendOtp("new-user@example.com");

        assertEquals("OTP sent to your email.", response);
        verify(otpRepository).deleteByEmail("new-user@example.com");

        ArgumentCaptor<Otp> otpCaptor = ArgumentCaptor.forClass(Otp.class);
        verify(otpRepository).save(otpCaptor.capture());
        verify(emailService).sendOTPEmail("new-user@example.com", "123456");

        Otp savedOtp = otpCaptor.getValue();
        assertEquals("new-user@example.com", savedOtp.getEmail());
        assertEquals("123456", savedOtp.getOtpCode());
        assertNotNull(savedOtp.getExpiresAt());
    }

    @Test
    void sendOtpShouldRejectVerifiedAccount() {
        User user = new User();
        user.setEmail("new-user@example.com");
        user.setVerified(true);
        when(userRepository.findByEmail("new-user@example.com")).thenReturn(user);

        MessageError exception = assertThrows(MessageError.class, () -> authService.sendOtp("new-user@example.com"));

        assertEquals("auth.error.accountAlreadyVerified", exception.getMessage());
        verify(otpRepository, never()).save(any(Otp.class));
    }

    @Test
    void verifyOtpShouldActivateUserAndDeleteConsumedOtp() {
        User user = new User();
        user.setEmail("new-user@example.com");
        user.setVerified(false);
        Otp otp = new Otp(1L, "new-user@example.com", "123456", LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByEmail("new-user@example.com")).thenReturn(user);
        when(otpRepository.findByEmailAndOtpCode("new-user@example.com", "123456")).thenReturn(Optional.of(otp));

        boolean result = authService.verifyOtp("new-user@example.com", "123456");

        assertTrue(result);
        assertTrue(user.isVerified());
        verify(userRepository).save(user);
        verify(otpRepository).delete(otp);
    }

    @Test
    void verifyOtpShouldReturnFalseWhenOtpIsMissingOrExpired() {
        User user = new User();
        user.setEmail("new-user@example.com");
        user.setVerified(false);
        when(userRepository.findByEmail("new-user@example.com")).thenReturn(user);
        when(otpRepository.findByEmailAndOtpCode("new-user@example.com", "000000")).thenReturn(Optional.empty());

        boolean missingOtpResult = authService.verifyOtp("new-user@example.com", "000000");

        assertFalse(missingOtpResult);

        Otp expiredOtp = new Otp(1L, "new-user@example.com", "123456", LocalDateTime.now().minusMinutes(1));
        when(otpRepository.findByEmailAndOtpCode("new-user@example.com", "123456")).thenReturn(Optional.of(expiredOtp));

        boolean expiredOtpResult = authService.verifyOtp("new-user@example.com", "123456");

        assertFalse(expiredOtpResult);
        verify(otpRepository).delete(expiredOtp);
    }

    @Test
    void authenticateShouldRejectUnverifiedAccount() {
        User user = new User();
        user.setEmail("new-user@example.com");
        user.setPassword(new BCryptPasswordEncoder(10).encode("Password1!"));
        user.setVerified(false);
        when(userRepository.count()).thenReturn(1L);
        when(userRepository.findByEmail("new-user@example.com")).thenReturn(user);

        MessageError exception = assertThrows(
                MessageError.class,
                () -> authService.authenticate(new org.example.demo.authentication.AuthenticationRequest("new-user@example.com", "Password1!"))
        );

        assertEquals("auth.error.accountNotVerified", exception.getMessage());
    }

    @Test
    void authenticateShouldSucceedForVerifiedAccount() {
        User user = new User();
        user.setEmail("new-user@example.com");
        user.setPassword(new BCryptPasswordEncoder(10).encode("Password1!"));
        user.setVerified(true);
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");
        user.setRoleSet(java.util.Set.of(role));
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "new-user@example.com",
                user.getPassword(),
                java.util.List.of(new SimpleGrantedAuthority("USER"))
        );

        when(userRepository.count()).thenReturn(1L);
        when(userRepository.findByEmail("new-user@example.com")).thenReturn(user);
        when(userDetailService.loadUserByUsername("new-user@example.com")).thenReturn(userDetails);

        assertNotNull(authService.authenticate(new org.example.demo.authentication.AuthenticationRequest("new-user@example.com", "Password1!")).getToken());
    }
}
