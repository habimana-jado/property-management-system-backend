package rw.afriteck.pms.stock.api;

import rw.afriteck.pms.stock.api.setup.RecipeCommand;
import rw.afriteck.pms.stock.api.setup.RecipeSummary;
import rw.afriteck.pms.stock.api.setup.RecipeView;
import rw.afriteck.pms.stock.api.view.PageResult;

import java.util.UUID;

public interface RecipeSetupApi {
    PageResult<RecipeSummary> recipes(String text, boolean activeOnly, int page, int size);
    RecipeView recipe(UUID id);
    RecipeView createRecipe(RecipeCommand command);
    RecipeView updateRecipe(UUID id, RecipeCommand command);
    void setRecipeActive(UUID id, boolean active);
}
