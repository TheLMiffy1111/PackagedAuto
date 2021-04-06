package thelm.packagedauto.api;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

public interface IPackageCraftingMachine {

	boolean acceptPackage(IPackageRecipeInfo recipeInfo, List<ItemStack> stacks, Direction direction);

	boolean isBusy();
}
