package thelm.packagedauto.integration.jei;

import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.resources.ResourceLocation;
import thelm.packagedauto.api.IRecipeSlotViewWrapper;
import thelm.packagedauto.api.IRecipeSlotsViewWrapper;

public class RecipeSlotsViewWrapper implements IRecipeSlotsViewWrapper {

	private final Object recipe;
	private final IRecipeSlotsView recipeSlotsView;

	public RecipeSlotsViewWrapper(Object recipe, IRecipeSlotsView recipeSlotsView) {
		this.recipe = recipe;
		this.recipeSlotsView = recipeSlotsView;
	}

	@Override
	public Object getRecipe() {
		return recipe;
	}

	@Override
	public List<IRecipeSlotViewWrapper> getRecipeSlotViews() {
		return Lists.transform(recipeSlotsView.getSlotViews(), RecipeSlotViewWrapper::new);
	}
}
