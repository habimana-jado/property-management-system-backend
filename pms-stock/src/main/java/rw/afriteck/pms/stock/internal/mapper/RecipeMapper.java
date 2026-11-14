package rw.afriteck.pms.stock.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import rw.afriteck.pms.stock.api.setup.RecipeLineView;
import rw.afriteck.pms.stock.api.setup.RecipeView;
import rw.afriteck.pms.stock.internal.model.Recipe;
import rw.afriteck.pms.stock.internal.model.RecipeLine;

import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecipeMapper {

    @Mapping(target = "lines", source = "lines", qualifiedByName = "linesByIngredientName")
    RecipeView toView(Recipe recipe);

    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientCode", source = "ingredient.code")
    @Mapping(target = "ingredientName", source = "ingredient.name")
    @Mapping(target = "unitId", source = "unit.id")
    @Mapping(target = "unitCode", source = "unit.code")
    RecipeLineView toLineView(RecipeLine line);

    /** Same order the API has always returned: by ingredient name. */
    @Named("linesByIngredientName")
    default List<RecipeLineView> linesByIngredientName(List<RecipeLine> lines) {
        return lines.stream()
                .sorted(Comparator.comparing(l -> l.getIngredient().getName()))
                .map(this::toLineView)
                .toList();
    }
}