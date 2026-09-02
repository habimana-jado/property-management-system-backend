package rw.afriteck.pms.model;

import jakarta.persistence.*;
import lombok.Data;
import rw.afriteck.pms.enums.EStatus;

import java.util.UUID;

@Entity
@Table(name = "hotels")
@Data
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID hotelId;
    private String hotelName;
    private String websiteUrl;
    private String logoUrl;
    private String slogan;
    @Enumerated(EnumType.STRING)
    private EStatus status;
}
