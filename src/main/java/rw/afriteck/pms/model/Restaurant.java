package rw.afriteck.pms.model;

import jakarta.persistence.*;
import lombok.Data;
import rw.afriteck.pms.enums.ERecordStatus;

import java.util.UUID;

@Entity
@Table(name = "restaurants")
@Data
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String restaurantName;
    private String tinNumber;
    @Enumerated(EnumType.STRING)
    private ERecordStatus status;

    @ManyToOne
    @JoinColumn(name = "hotel_branch_id")
    private HotelBranch hotelBranch;
}
