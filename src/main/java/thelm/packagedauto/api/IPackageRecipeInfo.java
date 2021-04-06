package thelm.packagedauto.api;

import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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

	List<ItemStack> getInputs();

	List<ItemStack> getOutputs();

	void generateFromStacks(List<ItemStack> input, List<ItemStack> output, World world);

	default Int2ObjectMap<ItemStack> getEncoderStacks() {
		return new Int2ObjectOpenHashMap<>();
	}

	@Override
	boolean equals(Object obj);

	@Override
	int hashCode();
}
