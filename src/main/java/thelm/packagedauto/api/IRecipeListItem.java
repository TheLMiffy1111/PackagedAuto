package thelm.packagedauto.api;

import net.minecraft.item.ItemStack;

public interface IRecipeListItem {

	IRecipeList getRecipeList(ItemStack stack);

	void setRecipeList(ItemStack stack, IRecipeList recipeList);
}
