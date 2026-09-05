package rw.afriteck.pms.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "menu_categories",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_menu_category_restaurant_name",
                columnNames = {"restaurant_id", "name"}
        )
)
@Data
public class MenuCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;
}
