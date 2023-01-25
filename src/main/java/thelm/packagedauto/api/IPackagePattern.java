package thelm.packagedauto.api;

import java.util.List;

import net.minecraft.item.ItemStack;

public interface IPackagePattern {

	IPackageRecipeInfo getRecipeInfo();

	int getIndex();

	List<ItemStack> getInputs();

	ItemStack getOutput();
}
