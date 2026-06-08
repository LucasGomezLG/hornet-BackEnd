package com.hornetimports.auth;

import com.hornetimports.auth.dto.GoogleLoginRequest;
import com.hornetimports.auth.dto.LoginResponse;
import com.hornetimports.config.JwtConfig;
import com.hornetimports.user.Profile;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GoogleAuthService googleAuthService;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final com.hornetimports.user.ProfileRepository profileRepository;
    private final JwtConfig jwtConfig;

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> loginWithGoogle(
            @Valid @RequestBody GoogleLoginRequest request,
            HttpServletResponse response) {

        Map<String, Object> payload = googleAuthService.verifyGoogleToken(request.idToken());
        String googleId = (String) payload.get("sub");
        String email = (String) payload.get("email");
        String name = (String) payload.getOrDefault("name", email);

        Profile profile = googleAuthService.findOrCreateProfile(googleId, email, name);

        String accessToken = jwtService.generateAccessToken(profile);
        String refreshToken = UUID.randomUUID().toString();

        RefreshToken rt = new RefreshToken();
        rt.setToken(refreshToken);
        rt.setUserId(profile.getId());
        rt.setExpiresAt(OffsetDateTime.now().plusSeconds(jwtConfig.getRefreshTokenExpiration() / 1000));
        refreshTokenRepository.save(rt);

        setRefreshCookie(response, refreshToken, (int)(jwtConfig.getRefreshTokenExpiration() / 1000));

        return ResponseEntity.ok(new LoginResponse(accessToken, profile.getTipo().name()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String token = extractRefreshCookie(request);
        if (token == null) {
            throw new BadCredentialsException("Sin refresh token");
        }

        RefreshToken rt = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadCredentialsException("Refresh token inválido"));

        if (rt.getExpiresAt().isBefore(OffsetDateTime.now())) {
            refreshTokenRepository.delete(rt);
            throw new BadCredentialsException("Refresh token expirado");
        }

        refreshTokenRepository.delete(rt);

        Profile profile = profileRepository.findById(rt.getUserId())
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        String newAccessToken = jwtService.generateAccessToken(profile);
        String newRefreshToken = UUID.randomUUID().toString();

        RefreshToken newRt = new RefreshToken();
        newRt.setToken(newRefreshToken);
        newRt.setUserId(profile.getId());
        newRt.setExpiresAt(OffsetDateTime.now().plusSeconds(jwtConfig.getRefreshTokenExpiration() / 1000));
        refreshTokenRepository.save(newRt);

        setRefreshCookie(response, newRefreshToken, (int)(jwtConfig.getRefreshTokenExpiration() / 1000));

        return ResponseEntity.ok(new LoginResponse(newAccessToken, profile.getTipo().name()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal Profile profile,
            HttpServletRequest request,
            HttpServletResponse response) {

        String token = extractRefreshCookie(request);
        if (token != null) {
            refreshTokenRepository.deleteById(token);
        }
        if (profile != null) {
            refreshTokenRepository.deleteByUserId(profile.getId());
        }

        setRefreshCookie(response, "", 0);
        return ResponseEntity.ok().build();
    }

    private void setRefreshCookie(HttpServletResponse response, String value, int maxAge) {
        Cookie cookie = new Cookie("refresh_token", value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true en producción (HTTPS)
        cookie.setPath("/api/auth");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private String extractRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> "refresh_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}