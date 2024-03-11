package thelm.packagedauto.api;

import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Please override {@link IPackageRecipeInfo#equals(IPackageRecipeInfo)} when implementing a new recipe type.
 */
public interface IPackageRecipeInfo {

	void load(CompoundTag nbt);

	void save(CompoundTag nbt);

	IPackageRecipeType getRecipeType();

	boolean isValid();

	List<IPackagePattern> getPatterns();

	List<ItemStack> getInputs();

	List<ItemStack> getOutputs();

	void generateFromStacks(List<ItemStack> input, List<ItemStack> output, Level level);

	Int2ObjectMap<ItemStack> getEncoderStacks();

	default ItemStack getCraftingRemainingItem(ItemStack stack) {
		if(getRecipeType().hasCraftingRemainingItem()) {
			return stack.getCraftingRemainingItem();
		}
		return ItemStack.EMPTY;
	}

	default boolean validPatternIndex(int index) {
		return index >= 0 && index < getPatterns().size();
	}

	@Override
	boolean equals(Object obj);

	@Override
	int hashCode();
}
