package thelm.packagedauto.api;

import net.minecraft.item.ItemStack;

public interface IPackageItem {

	IPackageRecipeInfo getRecipeInfo(ItemStack stack);

	int getIndex(ItemStack stack);
}
