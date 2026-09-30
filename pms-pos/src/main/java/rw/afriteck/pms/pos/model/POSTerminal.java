package rw.afriteck.pms.pos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.afriteck.pms.common.enums.ERecordStatus;

import java.util.UUID;

@Entity
@Table(name = "pos_terminal",
        uniqueConstraints = @UniqueConstraint(columnNames = {"hotel_branch_id", "code"}))
@Getter
@Setter
@NoArgsConstructor
public class POSTerminal {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "hotel_branch_id", nullable = false)
    private UUID hotelBranchId;

    @Column(nullable = false, length = 30)
    private String code; // e.g. "TILL-1", "COUNTER-A"

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ERecordStatus status = ERecordStatus.ACTIVE;
}
