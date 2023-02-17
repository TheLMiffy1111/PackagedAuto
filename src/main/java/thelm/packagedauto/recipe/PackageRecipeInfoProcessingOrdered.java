package thelm.packagedauto.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.util.MiscHelper;
import thelm.packagedauto.util.PackagePattern;

public class PackageRecipeInfoProcessingOrdered implements IPackageRecipeInfo {

	List<ItemStack> input = new ArrayList<>();
	List<ItemStack> output = new ArrayList<>();
	List<IPackagePattern> patterns = new ArrayList<>();

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		MiscHelper.INSTANCE.loadAllItems(nbt.getTagList("Input", 10), input);
		MiscHelper.INSTANCE.loadAllItems(nbt.getTagList("Output", 10), output);
		patterns.clear();
		for(int i = 0; i*9 < input.size(); ++i) {
			patterns.add(new PackagePattern(this, i, true));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		NBTTagList inputTag = MiscHelper.INSTANCE.saveAllItems(new NBTTagList(), input);
		nbt.setTag("Input", inputTag);
		NBTTagList outputTag = MiscHelper.INSTANCE.saveAllItems(new NBTTagList(), output);
		nbt.setTag("Output", outputTag);
		return nbt;
	}

	@Override
	public IPackageRecipeType getRecipeType() {
		return PackageRecipeTypeProcessingOrdered.INSTANCE;
	}

	@Override
	public boolean isValid() {
		return !input.isEmpty();
	}

	@Override
	public List<IPackagePattern> getPatterns() {
		return Collections.unmodifiableList(patterns);
	}

	@Override
	public List<ItemStack> getInputs() {
		return Collections.unmodifiableList(input);
	}

	@Override
	public List<ItemStack> getOutputs() {
		return Collections.unmodifiableList(output);
	}

	private static List<ItemStack> removeEmptyStacks(List<ItemStack> stacks) {
		return stacks.stream().filter(Objects::nonNull).collect(Collectors.toList());
	}

	@Override
	public void generateFromStacks(List<ItemStack> input, List<ItemStack> output, World world) {
		this.input.clear();
		this.input.addAll(removeEmptyStacks(input));
		this.output.clear();
		this.output.addAll(removeEmptyStacks(output));
		patterns.clear();
		for(int i = 0; i*9 < this.input.size(); ++i) {
			patterns.add(new PackagePattern(this, i, true));
		}
	}

	@Override
	public Map<Integer, ItemStack> getEncoderStacks() {
		Map<Integer, ItemStack> map = new HashMap<>();
		for(int i = 0; i < input.size(); ++i) {
			map.put(i, input.get(i));
		}
		for(int i = 0; i < output.size(); ++i) {
			map.put(i+81, output.get(i));
		}
		return map;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof PackageRecipeInfoProcessingOrdered) {
			PackageRecipeInfoProcessingOrdered other = (PackageRecipeInfoProcessingOrdered)obj;
			if(input.size() != other.input.size() || output.size() != other.output.size()) {
				return false;
			}
			for(int i = 0; i < input.size(); ++i) {
				if(!ItemStack.areItemStacksEqual(input.get(i), other.input.get(i))) {
					return false;
				}
				if(!ItemStack.areItemStackTagsEqual(input.get(i), other.input.get(i))) {
					return false;
				}
			}
			for(int i = 0; i < output.size(); ++i) {
				if(!ItemStack.areItemStacksEqual(output.get(i), other.output.get(i))) {
					return false;
				}
				if(!ItemStack.areItemStackTagsEqual(output.get(i), other.output.get(i))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		Object[] toHash = new Object[2];
		Object[] inputArray = new Object[input.size()];
		for(int i = 0; i < input.size(); ++i) {
			ItemStack stack = input.get(i);
			inputArray[i] = new Object[] {stack.getItem(), stack.getItemDamage(), stack.stackSize, stack.getTagCompound()};
		}
		Object[] outputArray = new Object[output.size()];
		for(int i = 0; i < output.size(); ++i) {
			ItemStack stack = output.get(i);
			outputArray[i] = new Object[] {stack.getItem(), stack.getItemDamage(), stack.stackSize, stack.getTagCompound()};
		}
		toHash[0] = inputArray;
		toHash[1] = outputArray;
		return Arrays.deepHashCode(toHash);
	}
}
