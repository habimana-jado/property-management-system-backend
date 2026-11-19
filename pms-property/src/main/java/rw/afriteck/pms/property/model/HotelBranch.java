package rw.afriteck.pms.property.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.afriteck.pms.common.enums.ERecordStatus;
import rw.afriteck.pms.common.tenant.TenantOwned;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "hotel_branches",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_hotel_branch_hotel_name",
                columnNames = {"hotel_id", "name"}
        ))
public class HotelBranch extends TenantOwned {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String location;
    private String contactNumber1;
    private String contactNumber2;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ERecordStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", insertable = false, updatable = false)
    private Hotel hotel;
}
