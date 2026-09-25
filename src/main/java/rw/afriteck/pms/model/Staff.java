package rw.afriteck.pms.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "staffs")
@Getter
@Setter
@NoArgsConstructor
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(length = 30)
    private String contactNumber;

    @Column(length = 150)
    private String email;

    @Column(length = 100)
    private String position;

    @Column(nullable = false)
    private LocalDate hireDate;

    @ManyToOne
    @JoinColumn(name = "hotel_branch_id")
    private HotelBranch hotelBranch;
}
