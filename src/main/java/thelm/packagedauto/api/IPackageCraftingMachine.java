package thelm.packagedauto.api;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public interface IPackageCraftingMachine {

	boolean acceptPackage(IPackageRecipeInfo recipeInfo, List<ItemStack> stacks, ForgeDirection side);

	boolean isBusy();
}
