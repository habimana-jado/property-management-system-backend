package rw.afriteck.pms.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "menu_categories")
@Data
public class MenuCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID menuCategoryId;
    private String name;
}
