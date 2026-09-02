package rw.afriteck.pms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.model.MenuCategory;

import java.util.UUID;

public interface MenuCategoryRepo extends JpaRepository<MenuCategory, UUID> {
}
