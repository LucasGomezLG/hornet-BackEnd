package com.hornetimports.auth;

import com.hornetimports.user.Profile;
import com.hornetimports.user.ProfileRepository;
import com.hornetimports.user.TipoCuenta;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final ProfileRepository profileRepository;
    private final RestTemplate restTemplate;

    @Value("${google.client-id}")
    private String googleClientId;

    @SuppressWarnings("unchecked")
    public Map<String, Object> verifyGoogleToken(String idToken) {
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        try {
            Map<String, Object> payload = restTemplate.getForObject(url, Map.class);
            if (payload == null) {
                throw new BadCredentialsException("Token de Google inválido");
            }
            String aud = (String) payload.get("aud");
            if (!googleClientId.equals(aud)) {
                throw new BadCredentialsException("Token no pertenece a esta aplicación");
            }
            return payload;
        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Error verificando token de Google: {}", e.getMessage());
            throw new BadCredentialsException("Token de Google inválido");
        }
    }

    @Transactional
    public Profile findOrCreateProfile(String googleId, String email, String name) {
        return profileRepository.findByGoogleId(googleId)
                .orElseGet(() -> profileRepository.findByEmail(email)
                        .map(existing -> {
                            existing.setGoogleId(googleId);
                            return profileRepository.save(existing);
                        })
                        .orElseGet(() -> {
                            Profile profile = new Profile();
                            profile.setGoogleId(googleId);
                            profile.setEmail(email);
                            profile.setNombre(name);
                            profile.setTipo(TipoCuenta.comprador);
                            return profileRepository.save(profile);
                        }));
    }
}