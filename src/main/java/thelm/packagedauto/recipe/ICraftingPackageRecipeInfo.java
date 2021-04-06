package thelm.packagedauto.recipe;

import java.util.Collections;
import java.util.List;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import thelm.packagedauto.api.IPackageRecipeInfo;

public interface ICraftingPackageRecipeInfo extends IPackageRecipeInfo {

	ItemStack getOutput();

	IRecipe getRecipe();

	CraftingInventory getMatrix();

	List<ItemStack> getRemainingItems();

	@Override
	default List<ItemStack> getOutputs() {
		return Collections.singletonList(getOutput());
	}
}
