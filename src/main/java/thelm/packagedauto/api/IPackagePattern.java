package thelm.packagedauto.api;

import java.util.List;

import net.minecraft.item.ItemStack;

public interface IPackagePattern {

	IRecipeInfo getRecipeInfo();

	int getIndex();

	List<ItemStack> getInputs();

	ItemStack getOutput();
}
