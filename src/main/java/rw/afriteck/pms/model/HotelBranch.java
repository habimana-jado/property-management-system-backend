package rw.afriteck.pms.model;

import jakarta.persistence.*;
import lombok.Data;
import rw.afriteck.pms.enums.ERecordStatus;

import java.util.UUID;

@Entity
@Table(name = "hotel_branches")
@Data
public class HotelBranch {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    private String location;
    private String contactNumber1;
    private String contactNumber2;
    @Enumerated(EnumType.STRING)
    private ERecordStatus status;

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;
}
