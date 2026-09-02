package rw.afriteck.pms.model;

import jakarta.persistence.*;
import lombok.Data;
import rw.afriteck.pms.enums.EStatus;

import java.util.UUID;

@Entity
@Table(name = "hotel_branches")
@Data
public class HotelBranch {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID hotelBranchId;
    private String name;
    private String location;
    private String contactNumber1;
    private String contactNumber2;
    @Enumerated(EnumType.STRING)
    private EStatus status;

    @ManyToOne
    private Hotel hotel;
}
