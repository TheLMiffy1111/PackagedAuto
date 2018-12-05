package thelm.packagedauto.api;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public interface IPackageCraftingMachine {

	boolean acceptPackage(IRecipeInfo recipeInfo, List<ItemStack> stacks, EnumFacing facing);

	boolean isBusy();
}
