package thelm.packagedauto.integration.jei;

import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.resources.ResourceLocation;
import thelm.packagedauto.api.IRecipeSlotViewWrapper;
import thelm.packagedauto.api.IRecipeSlotsViewWrapper;

public class RecipeSlotsViewWrapper implements IRecipeSlotsViewWrapper {

	private final ResourceLocation category;
	private final IRecipeSlotsView recipeSlotsView;

	public RecipeSlotsViewWrapper(ResourceLocation category, IRecipeSlotsView recipeSlotsView) {
		this.category = category;
		this.recipeSlotsView = recipeSlotsView;
	}

	@Override
	public ResourceLocation getCategoryUid() {
		return category;
	}

	@Override
	public List<IRecipeSlotViewWrapper> getRecipeSlotViews() {
		return Lists.transform(recipeSlotsView.getSlotViews(), RecipeSlotViewWrapper::new);
	}
}
