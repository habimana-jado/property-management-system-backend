package rw.afriteck.pms.stock.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.stock.internal.model.RecipeLine;

import java.util.UUID;

public interface RecipeLineRepository extends JpaRepository<RecipeLine, UUID> {
    boolean existsByIngredientId(UUID itemId);
    boolean existsByIngredientIdAndUnitId(UUID itemId, UUID unitId);
}
