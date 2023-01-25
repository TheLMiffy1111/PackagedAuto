package thelm.packagedauto.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Please override {@link IPackageRecipeInfo#equals(IPackageRecipeInfo)} when implementing a new recipe type.
 */
public interface IPackageRecipeInfo {

	void readFromNBT(NBTTagCompound nbt);

	NBTTagCompound writeToNBT(NBTTagCompound nbt);

	IPackageRecipeType getRecipeType();

	boolean isValid();

	List<IPackagePattern> getPatterns();

	List<ItemStack> getInputs();

	List<ItemStack> getOutputs();

	void generateFromStacks(List<ItemStack> input, List<ItemStack> output, World world);

	Map<Integer, ItemStack> getEncoderStacks();

	@Override
	boolean equals(Object obj);

	@Override
	int hashCode();
}
