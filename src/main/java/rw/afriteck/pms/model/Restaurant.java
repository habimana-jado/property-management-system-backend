package rw.afriteck.pms.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.afriteck.pms.enums.ERecordStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "restaurants",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_restaurant_hotel_branch_name",
                        columnNames = {"hotel_branch_id", "restaurant_name"}
                )
        }
)
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String restaurantName;
    @Column(nullable = false, unique = true)
    private String tinNumber;
    @Column(nullable = false, unique = true, length = 6)
    private String restaurantCode;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ERecordStatus status;
    private BigDecimal taxRate;

    @ManyToOne
    @JoinColumn(name = "hotel_branch_id")
    private HotelBranch hotelBranch;
}
