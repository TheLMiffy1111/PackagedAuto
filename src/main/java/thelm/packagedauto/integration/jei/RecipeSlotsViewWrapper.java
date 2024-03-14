package thelm.packagedauto.integration.jei;

import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import thelm.packagedauto.api.IRecipeSlotViewWrapper;
import thelm.packagedauto.api.IRecipeSlotsViewWrapper;

public record RecipeSlotsViewWrapper(Object recipe, IRecipeSlotsView recipeSlotsView) implements IRecipeSlotsViewWrapper {

	@Override
	public Object getRecipe() {
		return recipe;
	}

	@Override
	public List<IRecipeSlotViewWrapper> getRecipeSlotViews() {
		return Lists.transform(recipeSlotsView.getSlotViews(), RecipeSlotViewWrapper::new);
	}
}
