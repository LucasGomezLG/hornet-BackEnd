package com.hornetimports.user;

import com.hornetimports.user.dto.ActualizarPerfilRequest;
import com.hornetimports.user.dto.CambiarTipoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    public Profile getById(java.util.UUID id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new BadCredentialsException("Perfil no encontrado"));
    }

    public Profile actualizar(Profile profile, ActualizarPerfilRequest req) {
        if (req.nombre()    != null) profile.setNombre(req.nombre());
        if (req.apellido()  != null) profile.setApellido(req.apellido());
        if (req.telefono()  != null) profile.setTelefono(req.telefono());
        if (req.cuit()      != null) profile.setCuit(req.cuit());
        return profileRepository.save(profile);
    }

    public Profile cambiarTipo(Profile profile, CambiarTipoRequest req) {
        // Solo comprador puede subir a vendedor; admin no se puede cambiar desde aquí
        if (profile.getTipo() == TipoCuenta.admin) {
            throw new org.springframework.security.access.AccessDeniedException("No se puede cambiar el tipo de un admin");
        }
        if (req.tipo() == TipoCuenta.admin) {
            throw new org.springframework.security.access.AccessDeniedException("No se puede asignar tipo admin desde este endpoint");
        }
        profile.setTipo(req.tipo());
        return profileRepository.save(profile);
    }
}