package thelm.packagedauto.api;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IPackageRecipeListItem {

	IPackageRecipeList getRecipeList(World world, ItemStack stack);
}
