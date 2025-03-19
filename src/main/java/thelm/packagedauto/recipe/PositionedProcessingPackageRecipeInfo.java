package thelm.packagedauto.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.util.MiscHelper;
import thelm.packagedauto.util.PackagePattern;

public class PositionedProcessingPackageRecipeInfo implements IPositionedProcessingPackageRecipeInfo {

	List<ItemStack> input = new ArrayList<>();
	Int2ObjectMap<ItemStack> matrix = new Int2ObjectArrayMap<>(81);
	List<ItemStack> output = new ArrayList<>();
	List<IPackagePattern> patterns = new ArrayList<>();

	@Override
	public void read(CompoundNBT nbt) {
		input.clear();
		List<ItemStack> matrixList = new ArrayList<>();
		MiscHelper.INSTANCE.loadAllItems(nbt.getList("Matrix", 10), matrixList);
		for(int i = 0; i < 81 && i < matrixList.size(); ++i) {
			ItemStack stack = matrixList.get(i);
			if(!stack.isEmpty()) {
				matrix.put(i, stack);
				input.add(stack);
			}
		}
		MiscHelper.INSTANCE.loadAllItems(nbt.getList("Output", 10), output);
		for(int i = 0; i*9 < input.size(); ++i) {
			patterns.add(new PackagePattern(this, i));
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		List<ItemStack> matrixList = new ArrayList<>();
		for(int i = 0; i < 81; ++i) {
			matrixList.add(matrix.getOrDefault(i, ItemStack.EMPTY));
		}
		ListNBT matrixTag = MiscHelper.INSTANCE.saveAllItems(new ListNBT(), matrixList);
		nbt.put("Matrix", matrixTag);
		ListNBT outputTag = MiscHelper.INSTANCE.saveAllItems(new ListNBT(), output);
		nbt.put("Output", outputTag);
		return nbt;
	}

	@Override
	public IPackageRecipeType getRecipeType() {
		return PositionedProcessingPackageRecipeType.INSTANCE;
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

	@Override
	public Int2ObjectMap<ItemStack> getMatrix() {
		return matrix;
	}

	@Override
	public void generateFromStacks(List<ItemStack> input, List<ItemStack> output, World world) {
		this.input.clear();
		for(int i = 0; i < 81; ++i) {
			ItemStack stack = input.get(i).copy();
			if(!stack.isEmpty()) {
				matrix.put(i, stack);
				this.input.add(stack);
			}
		}
		this.output.clear();
		this.output.addAll(MiscHelper.INSTANCE.condenseStacks(output, true));
		patterns.clear();
		for(int i = 0; i*9 < this.input.size(); ++i) {
			patterns.add(new PackagePattern(this, i, true));
		}
	}

	@Override
	public Int2ObjectMap<ItemStack> getEncoderStacks() {
		Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
		map.putAll(matrix);
		for(int i = 0; i < output.size() && i < 3; ++i) {
			map.put(i*3+82, output.get(i));
		}
		return map;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof PositionedProcessingPackageRecipeInfo) {
			PositionedProcessingPackageRecipeInfo other = (PositionedProcessingPackageRecipeInfo)obj;
			return MiscHelper.INSTANCE.recipeEquals(this, null, other, null);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return MiscHelper.INSTANCE.recipeHashCode(this, null);
	}
}
