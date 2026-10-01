package rw.afriteck.pms.stock.internal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "stock_recipe_line",
        uniqueConstraints = @UniqueConstraint(columnNames = {"recipe_id", "ingredient_id"}))
@Getter
@Setter
public class RecipeLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private StockItem ingredient;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    // must be convertible to the ingredient's base unit (validated on save)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private UnitOfMeasure unit;
}
