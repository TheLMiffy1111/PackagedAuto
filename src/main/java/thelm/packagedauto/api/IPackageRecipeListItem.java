package thelm.packagedauto.api;

import net.minecraft.item.ItemStack;

public interface IPackageRecipeListItem {

	IPackageRecipeList getRecipeList(ItemStack stack);
}
