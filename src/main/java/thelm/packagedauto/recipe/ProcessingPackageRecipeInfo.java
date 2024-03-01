package thelm.packagedauto.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.util.MiscHelper;
import thelm.packagedauto.util.PackagePattern;

public class ProcessingPackageRecipeInfo implements IPackageRecipeInfo {

	List<ItemStack> input = new ArrayList<>();
	List<ItemStack> output = new ArrayList<>();
	List<IPackagePattern> patterns = new ArrayList<>();

	@Override
	public void read(CompoundNBT nbt) {
		MiscHelper.INSTANCE.loadAllItems(nbt.getList("Input", 10), input);
		MiscHelper.INSTANCE.loadAllItems(nbt.getList("Output", 10), output);
		patterns.clear();
		for(int i = 0; i*9 < input.size(); ++i) {
			patterns.add(new PackagePattern(this, i));
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		ListNBT inputTag = MiscHelper.INSTANCE.saveAllItems(new ListNBT(), input);
		nbt.put("Input", inputTag);
		ListNBT outputTag = MiscHelper.INSTANCE.saveAllItems(new ListNBT(), output);
		nbt.put("Output", outputTag);
		return nbt;
	}

	@Override
	public IPackageRecipeType getRecipeType() {
		return ProcessingPackageRecipeType.INSTANCE;
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
	public void generateFromStacks(List<ItemStack> input, List<ItemStack> output, World world) {
		this.input.clear();
		this.input.addAll(MiscHelper.INSTANCE.condenseStacks(input));
		this.output.clear();
		this.output.addAll(MiscHelper.INSTANCE.condenseStacks(output));
		patterns.clear();
		for(int i = 0; i*9 < this.input.size(); ++i) {
			patterns.add(new PackagePattern(this, i));
		}
	}

	@Override
	public Int2ObjectMap<ItemStack> getEncoderStacks() {
		Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
		for(int i = 0; i < input.size(); ++i) {
			map.put(i, input.get(i));
		}
		//TODO uncomment when AE2 support custom details again
		//for(int i = 0; i < output.size(); ++i) {
		//	map.put(i+81, output.get(i));
		//}
		for(int i = 0; i < output.size() && i < 3; ++i) {
			map.put(i*3+81+1, output.get(i));
		}
		return map;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ProcessingPackageRecipeInfo) {
			ProcessingPackageRecipeInfo other = (ProcessingPackageRecipeInfo)obj;
			return MiscHelper.INSTANCE.recipeEquals(this, null, other, null);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return MiscHelper.INSTANCE.recipeHashCode(this, null);
	}
}
