package thelm.packagedauto.api;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IPackageRecipeListItem {

	IPackageRecipeList getRecipeList(Level world, ItemStack stack);
}
