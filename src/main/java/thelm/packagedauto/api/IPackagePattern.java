package thelm.packagedauto.api;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

public interface IPackagePattern {

	IRecipeInfo getRecipeInfo();

	int getIndex();

	List<ItemStack> getInputs();

	ItemStack getOutput();
}
