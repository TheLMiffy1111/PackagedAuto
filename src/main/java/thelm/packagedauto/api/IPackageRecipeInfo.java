package thelm.packagedauto.api;

import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

/**
 * Please override {@link IPackageRecipeInfo#equals(IPackageRecipeInfo)} when implementing a new recipe type.
 */
public interface IPackageRecipeInfo {

	void read(CompoundNBT nbt);

	CompoundNBT write(CompoundNBT nbt);

	IPackageRecipeType getRecipeType();

	boolean isValid();

	List<IPackagePattern> getPatterns();

	default List<IPackagePattern> getExtraPatterns() {
		return Collections.emptyList();
	}

	List<ItemStack> getInputs();

	List<ItemStack> getOutputs();

	void generateFromStacks(List<ItemStack> input, List<ItemStack> output, World world);

	Int2ObjectMap<ItemStack> getEncoderStacks();

	default boolean validPatternIndex(int index) {
		return index >= 0 && index < getPatterns().size();
	}

	default boolean isPackageable() {
		return isValid() && !getInputs().isEmpty();
	}

	default boolean isCraftable() {
		return isPackageable() && !getOutputs().isEmpty();
	}

	@Override
	boolean equals(Object obj);

	@Override
	int hashCode();
}
