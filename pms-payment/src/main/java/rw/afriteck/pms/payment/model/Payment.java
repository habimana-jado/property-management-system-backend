package rw.afriteck.pms.payment.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.afriteck.pms.common.audit.Auditable;
import rw.afriteck.pms.common.enums.EPaymentMethod;
import rw.afriteck.pms.common.enums.EPayableType;
import rw.afriteck.pms.payment.enums.EPaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private EPaymentMethod method;

    @Enumerated(EnumType.STRING)
    private EPaymentStatus status;

    private Instant paidAt;

    private String reference;

    @Column(nullable = false)
    private UUID payableId;

    @Enumerated(EnumType.STRING)
    private EPayableType payableType;
}
