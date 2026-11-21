package rw.afriteck.pms.credit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.afriteck.pms.common.audit.Auditable;
import rw.afriteck.pms.common.tenant.AuditableTenantOwned;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "credit_customers",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_credit_customer_hotel_phone",
                columnNames = {"hotel_id", "phone_number"}))
@Getter
@Setter
@NoArgsConstructor
public class CreditCustomer extends AuditableTenantOwned {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber; // stored normalized (E.164)

    @Column(name = "credit_allowed", nullable = false)
    private boolean creditAllowed = true;

    @Column(name = "credit_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId;
}
