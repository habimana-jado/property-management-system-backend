package rw.afriteck.pms.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.afriteck.pms.enums.EPaymentMethod;
import rw.afriteck.pms.enums.EPaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

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

    @ManyToOne
    @JoinColumn(name = "table_bill_id", nullable = false)
    private TableBill tableBill;

}
