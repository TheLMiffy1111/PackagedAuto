package thelm.packagedauto.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IRecipeType;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.util.PatternHelper;

public class RecipeInfoProcessingPositioned implements IRecipeInfoProcessingPositioned {

	List<ItemStack> input = new ArrayList<>();
	Int2ObjectMap<ItemStack> matrix = new Int2ObjectArrayMap<>(81);
	List<ItemStack> output = new ArrayList<>();
	List<IPackagePattern> patterns = new ArrayList<>();

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		input.clear();
		List<ItemStack> matrixList = new ArrayList<>();
		MiscUtil.loadAllItems(nbt.getTagList("Matrix", 10), matrixList);
		for(int i = 0; i < 81 && i < matrixList.size(); ++i) {
			ItemStack stack = matrixList.get(i);
			if(!stack.isEmpty()) {
				matrix.put(i, stack);
				input.add(stack);
			}
		}
		MiscUtil.loadAllItems(nbt.getTagList("Output", 10), output);
		for(int i = 0; i*9 < input.size(); ++i) {
			patterns.add(new PatternHelper(this, i));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		List<ItemStack> matrixList = new ArrayList<>();
		for(int i = 0; i < 81; ++i) {
			matrixList.add(matrix.getOrDefault(i, ItemStack.EMPTY));
		}
		NBTTagList matrixTag = MiscUtil.saveAllItems(new NBTTagList(), matrixList);
		nbt.setTag("Matrix", matrixTag);
		NBTTagList outputTag = MiscUtil.saveAllItems(new NBTTagList(), output);
		nbt.setTag("Output", outputTag);
		return nbt;
	}

	@Override
	public IRecipeType getRecipeType() {
		return RecipeTypeProcessingPositioned.INSTANCE;
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
		this.output.addAll(MiscUtil.condenseStacks(output, true));
		patterns.clear();
		for(int i = 0; i*9 < this.input.size(); ++i) {
			patterns.add(new PatternHelper(this, i));
		}
	}

	@Override
	public Int2ObjectMap<ItemStack> getEncoderStacks() {
		Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
		map.putAll(matrix);
		for(int i = 0; i < output.size(); ++i) {
			map.put(i+81, output.get(i));
		}
		return map;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RecipeInfoProcessingPositioned) {
			RecipeInfoProcessingPositioned other = (RecipeInfoProcessingPositioned)obj;
			return MiscUtil.recipeEquals(this, null, other, null);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return MiscUtil.recipeHashCode(this, null);
	}
}
