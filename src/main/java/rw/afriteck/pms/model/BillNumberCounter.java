package rw.afriteck.pms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bill_number_counters")
public class BillNumberCounter {

    @Id
    @Column(name = "restaurant_id")
    private UUID restaurantId;

    @Column(name = "last_number", nullable = false)
    private Long lastNumber = 0L;

}
