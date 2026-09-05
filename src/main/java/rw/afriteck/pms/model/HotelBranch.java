package rw.afriteck.pms.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.afriteck.pms.enums.ERecordStatus;

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
public class HotelBranch {
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

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;
}
