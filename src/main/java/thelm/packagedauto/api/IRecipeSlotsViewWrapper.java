package thelm.packagedauto.api;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

public interface IRecipeSlotsViewWrapper {

	Object getRecipe();

	List<IRecipeSlotViewWrapper> getRecipeSlotViews();
}
