package thelm.packagedauto.recipe;

import java.util.List;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import thelm.packagedauto.api.IPackageRecipeInfo;

public interface ICraftingPackageRecipeInfo extends IPackageRecipeInfo {

	ItemStack getOutput();

	CraftingRecipe getRecipe();

	CraftingContainer getMatrix();

	List<ItemStack> getRemainingItems();

	@Override
	default List<ItemStack> getOutputs() {
		return List.of(getOutput());
	}
}
