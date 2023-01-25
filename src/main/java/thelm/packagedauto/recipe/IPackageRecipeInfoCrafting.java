package thelm.packagedauto.recipe;

import java.util.Collections;
import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import thelm.packagedauto.api.IPackageRecipeInfo;

public interface IPackageRecipeInfoCrafting extends IPackageRecipeInfo {

	ItemStack getOutput();

	IRecipe getRecipe();

	InventoryCrafting getMatrix();

	@Override
	default List<ItemStack> getOutputs() {
		return Collections.singletonList(getOutput());
	}
}
