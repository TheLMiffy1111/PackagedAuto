package thelm.packagedauto.integration.jei;

import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import thelm.packagedauto.api.IRecipeSlotViewWrapper;
import thelm.packagedauto.api.IRecipeSlotsViewWrapper;

public class RecipeSlotsViewWrapper implements IRecipeSlotsViewWrapper {

	private final IRecipeSlotsView recipeSlotsView;

	public RecipeSlotsViewWrapper(IRecipeSlotsView recipeSlotsView) {
		this.recipeSlotsView = recipeSlotsView;
	}

	@Override
	public List<IRecipeSlotViewWrapper> getRecipeSlotViews() {
		return Lists.transform(recipeSlotsView.getSlotViews(), RecipeSlotViewWrapper::new);
	}
}
