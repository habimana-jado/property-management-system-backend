package rw.afriteck.pms.pos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "pos_sale_item")
@Getter
@Setter
@NoArgsConstructor
public class POSSaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price_at_sale_time", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPriceAtSaleTime;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "line_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;

    @ManyToOne
    @JoinColumn(name = "sale_id", nullable = false)
    private POSSale sale;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private POSProduct product;

}
