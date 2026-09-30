package rw.afriteck.pms.pos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "pos_sale_number_counter")
@Setter
public class POSSaleNumberCounter {

    @Id
    private UUID hotelBranchId;

    @Column(nullable = false)
    private long lastNumber = 0;

    public long incrementAndGet() {
        return ++lastNumber;
    }
}
