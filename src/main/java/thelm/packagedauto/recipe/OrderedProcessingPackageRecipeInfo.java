package thelm.packagedauto.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.util.MiscHelper;
import thelm.packagedauto.util.PackagePattern;

public class OrderedProcessingPackageRecipeInfo implements IPackageRecipeInfo {

	List<ItemStack> input = new ArrayList<>();
	List<ItemStack> output = new ArrayList<>();
	List<IPackagePattern> patterns = new ArrayList<>();

	@Override
	public void load(CompoundTag nbt) {
		MiscHelper.INSTANCE.loadAllItems(nbt.getList("Input", 10), input);
		MiscHelper.INSTANCE.loadAllItems(nbt.getList("Output", 10), output);
		patterns.clear();
		for(int i = 0; i*9 < input.size(); ++i) {
			patterns.add(new PackagePattern(this, i, true));
		}
	}

	@Override
	public void save(CompoundTag nbt) {
		ListTag inputTag = MiscHelper.INSTANCE.saveAllItems(new ListTag(), input);
		nbt.put("Input", inputTag);
		ListTag outputTag = MiscHelper.INSTANCE.saveAllItems(new ListTag(), output);
		nbt.put("Output", outputTag);
	}

	@Override
	public IPackageRecipeType getRecipeType() {
		return OrderedProcessingPackageRecipeType.INSTANCE;
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
		return stacks.stream().filter(stack -> !stack.isEmpty()).collect(Collectors.toList());
	}

	@Override
	public void generateFromStacks(List<ItemStack> input, List<ItemStack> output, Level level) {
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
	public Int2ObjectMap<ItemStack> getEncoderStacks() {
		Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
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
		if(obj instanceof OrderedProcessingPackageRecipeInfo other) {
			if(input.size() != other.input.size() || output.size() != other.output.size()) {
				return false;
			}
			for(int i = 0; i < input.size(); ++i) {
				if(!ItemStack.matches(input.get(i), other.input.get(i))) {
					return false;
				}
			}
			for(int i = 0; i < output.size(); ++i) {
				if(!ItemStack.matches(output.get(i), other.output.get(i))) {
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
			inputArray[i] = new Object[] {stack.getItem(), stack.getCount(), stack.getTag()};
		}
		Object[] outputArray = new Object[output.size()];
		for(int i = 0; i < output.size(); ++i) {
			ItemStack stack = output.get(i);
			outputArray[i] = new Object[] {stack.getItem(), stack.getCount(), stack.getTag()};
		}
		toHash[0] = inputArray;
		toHash[1] = outputArray;
		return Arrays.deepHashCode(toHash);
	}
}
