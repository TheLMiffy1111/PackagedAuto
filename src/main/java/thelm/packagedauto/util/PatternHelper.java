package thelm.packagedauto.util;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.item.ItemStack;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.item.ItemPackage;

public class PatternHelper implements IPackagePattern {

	public final IRecipeInfo recipeInfo;
	public final int index;
	public final List<ItemStack> inputs;
	public final ItemStack output;

	public PatternHelper(IRecipeInfo recipeInfo, int index) {
		this(recipeInfo, index, false);
	}

	public PatternHelper(IRecipeInfo recipeInfo, int index, boolean condense) {
		this.recipeInfo = recipeInfo;
		this.index = index;
		List<ItemStack> recipeInputs = recipeInfo.getInputs();
		recipeInputs = recipeInputs.subList(9*index, Math.min(9*index+9, recipeInputs.size()));
		if(condense) {
			inputs = ImmutableList.copyOf(MiscUtil.condenseStacks(recipeInputs));
		}
		else {
			inputs = ImmutableList.copyOf(recipeInputs);
		}
		output = ItemPackage.makePackage(recipeInfo, index);
	}

	@Override
	public IRecipeInfo getRecipeInfo() {
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
