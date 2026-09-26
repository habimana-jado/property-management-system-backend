package rw.afriteck.pms.property.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.afriteck.pms.common.enums.ERecordStatus;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
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
