package rw.afriteck.pms.stock.internal.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.stock.internal.model.Recipe;

import java.util.Optional;
import java.util.UUID;

public interface RecipeRepository extends JpaRepository<Recipe, UUID> {
    @EntityGraph(attributePaths = {"lines", "lines.ingredient", "lines.ingredient.baseUnit", "lines.unit"})
    Optional<Recipe> findWithLinesById(UUID id);
}
