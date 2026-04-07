package org.example.demo.service.impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import org.example.demo.authentication.*;
import org.example.demo.entities.Otp;
import org.example.demo.entities.Role;
import org.example.demo.entities.User;
import org.example.demo.exception.ApiException;
import org.example.demo.exception.ErrorCode;
import org.example.demo.exception.MessageError;
import org.example.demo.repositories.OtpRepository;
import org.example.demo.repositories.RoleRepository;
import org.example.demo.repositories.UserRepository;
import org.example.demo.security.UserDetailService;
import org.example.demo.service.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthService implements IAuthService {
    private String SIGNER_KEY = "xJTUVPssT2uOmsKKn7dmwi/ZuUg1b8ECkPuoSBYziE4ldzMBtSNkaftz1U372J/e";
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserDetailService userDetailService;
    @Autowired
    private OtpRepository otpRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private EmailService emailService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        if (userRepository.count() == 0) {
            throw new ApiException(ErrorCode.UNAUTHENTICATION);
        }
        User user = userRepository.findByEmail(authenticationRequest.getEmail());
        UserDetails userDetails = userDetailService.loadUserByUsername(authenticationRequest.getEmail());
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        if (user == null || !passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword())) {
            throw new ApiException(ErrorCode.UNAUTHENTICATION);
        }
        return AuthenticationResponse.builder()
                .email(userDetails.getUsername())  // Sử dụng thông tin từ UserDetails
                .roleList(userDetails.getAuthorities())
                .token(generateToken(user))    // Tạo token cho UserDetails
                .build();
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        List<String> roles = user.getRoleSet()
                .stream()
                .map(Role::getName) // ví dụ role.getName() trả về "ADMIN"
                .collect(Collectors.toList());


        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("vhh.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(7, ChronoUnit.DAYS).toEpochMilli()))
                .claim("roles", roles) // <== thêm claim "roles"
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()) != null) {
            throw new MessageError("Email is already in use");
        }

        Otp otp = getValidOtp(registerRequest.getEmail(), registerRequest.getOtp());

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("USER");
        roles.add(userRole);
        user.setRoleSet(roles);
        userRepository.save(user);

        otpRepository.delete(otp);

        return RegisterResponse.builder()
                .email(registerRequest.getEmail())
                .build();
    }

    @Override
    public String sendOtp(String email) {
        otpRepository.deleteByEmail(email);

        String code = emailService.generateOTP();

        Otp otp = new Otp();
        otp.setEmail(email);
        otp.setOtpCode(code);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(otp);

        emailService.sendOTPEmail(email, code);

        return "OTP sent to your email.";
    }

    public String requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return "Email not found!";
        }

        otpRepository.deleteByEmail(email);

        String code = emailService.generateOTP();
        emailService.sendOTPEmail(email, code);

        Otp otp = new Otp();
        otp.setEmail(email);
        otp.setOtpCode(code);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(otp);

        return "OTP sent to your email. Please verify to reset your password.";
    }

    @Override
    public Boolean verifyOtp(String email, String code) {
        Optional<Otp> otpOptional = otpRepository.findByEmailAndOtpCode(email, code);
        if (otpOptional.isEmpty()) {
            return false;
        }

        Otp otp = otpOptional.get();
        if (isExpired(otp)) {
            otpRepository.delete(otp);
            return false;
        }

        return true;
    }

    public String changePassword(RequestPasswordReset requestPasswordReset) {
        User user = userRepository.findByEmail(requestPasswordReset.getEmail());
        user.setPassword(passwordEncoder.encode(requestPasswordReset.getPassword()));
        userRepository.save(user);
        return "Password changed successfully!";
    }

    private Otp getValidOtp(String email, String code) {
        Optional<Otp> otpOptional = otpRepository.findByEmailAndOtpCode(email, code);
        if (otpOptional.isEmpty()) {
            throw new MessageError("Invalid OTP");
        }

        Otp otp = otpOptional.get();
        if (isExpired(otp)) {
            otpRepository.delete(otp);
            throw new MessageError("OTP has expired");
        }

        return otp;
    }

    private boolean isExpired(Otp otp) {
        return otp.getExpiresAt() == null || otp.getExpiresAt().isBefore(LocalDateTime.now());
    }
}
