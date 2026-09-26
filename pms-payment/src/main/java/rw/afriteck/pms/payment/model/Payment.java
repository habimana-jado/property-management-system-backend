package rw.afriteck.pms.payment.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.afriteck.pms.common.audit.Auditable;
import rw.afriteck.pms.payment.enums.EPaymentMethod;
import rw.afriteck.pms.payment.enums.EPaymentSourceType;
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
    //TODO Rename to meaningful Online/Momo Transaction Reference ID
    private String reference;

    @Column(nullable = false)
    private UUID sourceReferenceId;      // e.g. the RestaurantOrder id or RoomBooking id

    @Enumerated(EnumType.STRING)
    private EPaymentSourceType sourceType;
}
