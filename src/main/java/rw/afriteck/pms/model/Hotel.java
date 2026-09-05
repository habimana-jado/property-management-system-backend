package rw.afriteck.pms.model;

import jakarta.persistence.*;
import lombok.Data;
import rw.afriteck.pms.enums.ERecordStatus;

import java.util.UUID;

@Entity
@Table(name = "hotels",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_hotel_email",
                        columnNames = "email"
                ),
                @UniqueConstraint(
                        name = "uk_hotel_name",
                        columnNames = "hotel_name"
                )
        }
)
@Data
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String hotelName;
    @Column(nullable = false, unique = true)
    private String email;
    private String websiteUrl;
    private String logoUrl;
    private String slogan;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ERecordStatus status;
}
