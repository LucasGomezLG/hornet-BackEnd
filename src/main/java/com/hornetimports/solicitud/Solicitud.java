package com.hornetimports.solicitud;

import com.hornetimports.user.Profile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "solicitudes")
@Getter @Setter @NoArgsConstructor
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Profile user;

    @Column(nullable = false)
    private String estado = "pendiente";

    @Column(name = "nota_cliente")
    private String notaCliente;

    @Column(name = "nota_admin")
    private String notaAdmin;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SolicitudItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean isPendiente() {
        return "pendiente".equals(estado);
    }

    public boolean isCotizada() {
        return "cotizada".equals(estado);
    }
}
