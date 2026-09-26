package rw.afriteck.pms.restaurant.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "menu_categories",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_menu_category_restaurant_name",
                columnNames = {"restaurant_id", "name"}
        )
)
public class MenuCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String name;
    private Integer displayOrder; // for consistent, business-controlled ordering

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;
}
