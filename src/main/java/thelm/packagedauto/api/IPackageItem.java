package thelm.packagedauto.api;

import net.minecraft.item.ItemStack;

public interface IPackageItem {

	IRecipeInfo getRecipeInfo(ItemStack stack);

	int getIndex(ItemStack stack);
}
