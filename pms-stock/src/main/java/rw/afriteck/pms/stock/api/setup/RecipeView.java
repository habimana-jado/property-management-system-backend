package rw.afriteck.pms.stock.api.setup;

import java.util.List;
import java.util.UUID;

public record RecipeView(UUID id, String name, boolean active, List<RecipeLineView> lines) {}

