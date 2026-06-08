package com.hornetimports.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    Optional<Profile> findByGoogleId(String googleId);
    Optional<Profile> findByEmail(String email);
    List<Profile> findByTipo(TipoCuenta tipo);
    long countByTipo(TipoCuenta tipo);
    List<Profile> findByIdIn(Collection<UUID> ids);
}