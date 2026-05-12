package com.eventra.service;

import com.eventra.dto.request.*;
import com.eventra.dto.response.AccessTokenResponse;
import com.eventra.dto.response.AuthResponse;
import com.eventra.entity.*;
import com.eventra.entity.enums.UserStatus;
import com.eventra.exception.*;
import com.eventra.repository.*;
import com.eventra.security.JwtService;
import com.eventra.util.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpirationMs;

    // ─────────────────────────────────────────────────────────
    // REGISTER
    // ─────────────────────────────────────────────────────────

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(
                request.email().toLowerCase().strip())) {
            throw new DuplicateEmailException();
        }

        User user = User.builder()
                .fullName(request.fullName().strip())
                .email(request.email().toLowerCase().strip())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();

        userRepository.save(user);
        log.info("Utilizator înregistrat: {} rol={}", user.getId(), user.getRole());

        String rawToken = UUID.randomUUID().toString();
        createAndSaveEmailVerificationToken(user, rawToken);
        emailService.sendVerificationEmail(
                user.getEmail(), user.getFullName(), rawToken);
    }

    // ─────────────────────────────────────────────────────────
    // VERIFY EMAIL
    // ─────────────────────────────────────────────────────────

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        String tokenHash = TokenHashUtil.hash(request.token());

        EmailVerificationToken token = emailVerificationTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException(
                        "Token-ul de verificare este invalid sau a expirat."));

        if (!token.isValid()) {
            throw new InvalidTokenException(
                    "Token-ul de verificare este invalid sau a expirat.");
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        emailVerificationTokenRepository.save(token);

        emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
        log.info("Email verificat: userId={}", user.getId());
    }

    // ─────────────────────────────────────────────────────────
    // RESEND VERIFICATION
    // ─────────────────────────────────────────────────────────

    @Transactional
    public void resendVerificationEmail(String email) {
        // Răspuns identic indiferent dacă emailul există — anti-enumeration
        userRepository.findByEmail(email.toLowerCase().strip())
                .filter(user -> !user.isEmailVerified())
                .ifPresent(user -> {
                    emailVerificationTokenRepository.deleteByUserId(user.getId());
                    String rawToken = UUID.randomUUID().toString();
                    createAndSaveEmailVerificationToken(user, rawToken);
                    emailService.sendVerificationEmail(
                            user.getEmail(), user.getFullName(), rawToken);
                    log.info("Email verificare retrims: userId={}", user.getId());
                });
    }

    // ─────────────────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository
                .findByEmail(request.email().toLowerCase().strip())
                .orElseThrow(() -> new UnauthorizedException(
                        "Email sau parolă incorectă."));

        if (!passwordEncoder.matches(
                request.password(), user.getPasswordHash())) {
            log.warn("Parolă incorectă pentru: {}", request.email());
            throw new UnauthorizedException("Email sau parolă incorectă.");
        }

        if (!user.isEmailVerified()) {
            throw new UnauthorizedException(
                    "Contul nu a fost verificat. Verifică-ți emailul.");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException(
                    "Contul este suspendat sau inactiv.");
        }

        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getRole());
        String rawRefreshToken = UUID.randomUUID().toString();
        createAndSaveRefreshToken(user, rawRefreshToken);

        log.info("Login reușit: userId={}", user.getId());
        return new AuthResponse(
                accessToken,
                rawRefreshToken,
                user.getRole(),
                user.getId(),
                user.getFullName()
        );
    }

    // ─────────────────────────────────────────────────────────
    // REFRESH TOKEN
    // ─────────────────────────────────────────────────────────

    @Transactional
    public AccessTokenResponse refresh(RefreshTokenRequest request) {
        String tokenHash = TokenHashUtil.hash(request.refreshToken());

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException(
                        "Refresh token invalid sau expirat."));

        if (!refreshToken.isValid()) {
            throw new InvalidTokenException(
                    "Refresh token invalid sau expirat.");
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateAccessToken(
                user.getId(), user.getRole());

        log.info("Access token reînnoit: userId={}", user.getId());
        return new AccessTokenResponse(newAccessToken);
    }

    // ─────────────────────────────────────────────────────────
    // LOGOUT
    // ─────────────────────────────────────────────────────────

    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("Logout: toate refresh token-urile revocate pentru userId={}",
                userId);
    }

    // ─────────────────────────────────────────────────────────
    // FORGOT PASSWORD
    // ─────────────────────────────────────────────────────────

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Răspuns identic indiferent dacă emailul există — anti-enumeration
        userRepository.findByEmail(request.email().toLowerCase().strip())
                .ifPresent(user -> {
                    passwordResetTokenRepository.deleteByUserId(user.getId());
                    String rawToken = UUID.randomUUID().toString();
                    createAndSavePasswordResetToken(user, rawToken);
                    emailService.sendPasswordResetEmail(
                            user.getEmail(), user.getFullName(), rawToken);
                    log.info("Email reset parolă trimis: userId={}", user.getId());
                });
    }

    // ─────────────────────────────────────────────────────────
    // RESET PASSWORD
    // ─────────────────────────────────────────────────────────

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = TokenHashUtil.hash(request.token());

        PasswordResetToken token = passwordResetTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException(
                        "Token-ul de resetare este invalid sau a expirat."));

        if (!token.isValid()) {
            throw new InvalidTokenException(
                    "Token-ul de resetare este invalid sau a expirat.");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(token);

        // Revocăm toate sesiunile active după reset parolă
        refreshTokenRepository.revokeAllByUserId(user.getId());

        log.info("Parolă resetată pentru userId={}", user.getId());
    }

    // ─────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────

    private void createAndSaveEmailVerificationToken(User user,
                                                     String rawToken) {
        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(user)
                .tokenHash(TokenHashUtil.hash(rawToken))
                .expiresAt(Instant.now().plusSeconds(86400)) // 24 ore
                .build();
        emailVerificationTokenRepository.save(token);
    }

    private void createAndSaveRefreshToken(User user, String rawToken) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(TokenHashUtil.hash(rawToken))
                .expiresAt(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build();
        refreshTokenRepository.save(token);
    }

    private void createAndSavePasswordResetToken(User user, String rawToken) {
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash(TokenHashUtil.hash(rawToken))
                .expiresAt(Instant.now().plusSeconds(3600)) // 1 oră
                .build();
        passwordResetTokenRepository.save(token);
    }
}