package thelm.packagedauto.api;

import net.minecraft.world.item.ItemStack;

public interface IPackageRecipeListItem {

	IPackageRecipeList getRecipeList(ItemStack stack);

	void setRecipeList(ItemStack stack, IPackageRecipeList recipeList);
}
