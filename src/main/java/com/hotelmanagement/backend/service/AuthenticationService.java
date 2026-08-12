package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.request.*;
import com.hotelmanagement.backend.dto.response.AuthenticationResponse;
import com.hotelmanagement.backend.dto.response.IntrospectResponse;
import com.hotelmanagement.backend.entity.InvalidatedToken;
import com.hotelmanagement.backend.entity.PasswordResetToken;
import com.hotelmanagement.backend.entity.Role;
import com.hotelmanagement.backend.entity.User;
import com.hotelmanagement.backend.exception.AppException;
import com.hotelmanagement.backend.enums.ErrorCode;
import com.hotelmanagement.backend.enums.BookingEmailLocale;
import com.hotelmanagement.backend.mapper.UserMapper;
import com.hotelmanagement.backend.repository.InvalidatedTokenRepository;
import com.hotelmanagement.backend.repository.PasswordResetTokenRepository;
import com.hotelmanagement.backend.repository.UserRepository;
import com.hotelmanagement.backend.template.PasswordResetEmailTemplate;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    PasswordResetTokenRepository passwordResetTokenRepository;
    EmailService emailService;

    @NonFinal
    @Value("${app.cors.origin}")
    String FRONTEND_ORIGIN;

    @NonFinal
    @Value("${jwt.signer-key}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected Long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected Long REFRESHABLE_DURATION;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!user.isActive()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if(!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .user(userMapper.toUserShortResponse(user))
                .build();
    }
    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("anhkhoa.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }
    public IntrospectResponse introspect(IntrospectRequest request) {
        try{
            var token = request.getToken();
            boolean isValid = true;
            try {
                verifyToken(token, false);
            }catch (AppException e){
                isValid= false;
            }

            return IntrospectResponse.builder()
                    .valid(isValid)
                    .build();
        }catch (AppException | JOSEException e){
            log.error("Can't introspect token: ", e);
            throw new RuntimeException(e);
        } catch (ParseException e) {
            log.error("Can't parse JWT: ", e);
            throw new RuntimeException(e);
        }
    }

    String buildScope(User user) {
        StringJoiner scope = new StringJoiner(" ");
        if (user.getRole() != null) {
            Role role = user.getRole();
            scope.add("ROLE_" + role.getName());
            if(!CollectionUtils.isEmpty(role.getPermissions())){
                role.getPermissions().forEach(permission -> scope.add(permission.getName()));
            }
        }
        return scope.toString();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try{
            var signToken = verifyToken(request.getToken(), true);
            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jit)
                    .expiryTime(expiryTime)
                    .build();

            invalidatedTokenRepository.save(invalidatedToken);
        }catch (AppException e){
            log.info("Token already expired");
        }
    }
    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();
        invalidatedTokenRepository.save(invalidatedToken);

        var userId = signedJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }
    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT
                .getJWTClaimsSet()
                .getIssueTime()
                .toInstant()
                .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date())))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    public void requestPasswordReset(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String token = generatePasswordResetToken();

        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .token(token)
                .expirationDateTime(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();

        passwordResetTokenRepository.save(passwordResetToken);

        String resetUrl =
                FRONTEND_ORIGIN + "/reset-password?token=" + token;

        BookingEmailLocale locale = request.getLocale() == null
                ? BookingEmailLocale.VI
                : request.getLocale();
        String html = PasswordResetEmailTemplate.build(resetUrl, locale);

        emailService.sendHtmlEmail(
                user.getEmail(),
                PasswordResetEmailTemplate.subject(locale),
                html
        );
    }

    private String generatePasswordResetToken() {
        String token;
        do {
            token = String.format("%06d", new Random().nextInt(1_000_000));
        } while (passwordResetTokenRepository.existsByToken(token));

        return token;
    }

    public void resetPassword(PasswordResetConfirmRequest request) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if (passwordResetToken.getExpirationDateTime().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(passwordResetToken);
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        passwordResetTokenRepository.delete(passwordResetToken);
    }
}
