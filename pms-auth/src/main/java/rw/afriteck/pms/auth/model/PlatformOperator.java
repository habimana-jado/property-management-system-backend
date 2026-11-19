package rw.afriteck.pms.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.afriteck.pms.auth.enums.EPlatformRole;
import rw.afriteck.pms.common.audit.Auditable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "platform_operators")
@Getter
@Setter
@NoArgsConstructor
public class PlatformOperator extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EPlatformRole role;

    @Column(nullable = false)
    private boolean enabled = true;

    private Instant lastLoginAt;
}
