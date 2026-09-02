package rw.afriteck.pms.model;

import jakarta.persistence.*;
import lombok.Data;
import rw.afriteck.pms.enums.EStatus;

import java.util.UUID;

@Entity
@Table(name = "restaurants")
@Data
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID restaurantId;
    private String restaurantName;
    private String tinNumber;
    @Enumerated(EnumType.STRING)
    private EStatus status;

    @ManyToOne
    private HotelBranch hotelBranch;
}
