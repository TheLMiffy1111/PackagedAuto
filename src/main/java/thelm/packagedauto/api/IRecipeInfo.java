package thelm.packagedauto.api;

import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Please override {@link IRecipeInfo#equals(IRecipeInfo)} when implementing a new recipe type.
 */
public interface IRecipeInfo {

	void readFromNBT(NBTTagCompound nbt);

	NBTTagCompound writeToNBT(NBTTagCompound nbt);

	IRecipeType getRecipeType();

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
