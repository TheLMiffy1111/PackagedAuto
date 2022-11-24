package thelm.packagedauto.util;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.item.PackageItem;

public class PackagePattern implements IPackagePattern {

	public final IPackageRecipeInfo recipeInfo;
	public final int index;
	public final List<ItemStack> inputs;
	public final ItemStack output;

	public PackagePattern(IPackageRecipeInfo recipeInfo, int index) {
		this(recipeInfo, index, false);
	}

	public PackagePattern(IPackageRecipeInfo recipeInfo, int index, boolean condense) {
		this.recipeInfo = recipeInfo;
		this.index = index;
		List<ItemStack> recipeInputs = recipeInfo.getInputs();
		recipeInputs = recipeInputs.subList(9*index, Math.min(9*index+9, recipeInputs.size()));
		if(condense) {
			inputs = List.copyOf(MiscHelper.INSTANCE.condenseStacks(recipeInputs));
		}
		else {
			inputs = List.copyOf(recipeInputs);
		}
		output = PackageItem.makePackage(recipeInfo, index);
	}

	@Override
	public IPackageRecipeInfo getRecipeInfo() {
		return recipeInfo;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public List<ItemStack> getInputs() {
		return inputs;
	}

	@Override
	public ItemStack getOutput() {
		return output.copy();
	}
}
