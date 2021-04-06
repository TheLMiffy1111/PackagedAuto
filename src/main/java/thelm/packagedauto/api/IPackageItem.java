package thelm.packagedauto.api;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IPackageItem {

	IPackageRecipeInfo getRecipeInfo(ItemStack stack);

	int getIndex(ItemStack stack);
}
