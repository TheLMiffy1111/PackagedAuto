package thelm.packagedauto.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.util.MiscHelper;
import thelm.packagedauto.util.PackagePattern;

public class PositionedProcessingPackageRecipeInfo implements IPositionedProcessingPackageRecipeInfo {

	public static final MapCodec<PositionedProcessingPackageRecipeInfo> MAP_CODEC = RecordCodecBuilder.mapCodec(instance->instance.group(
			ItemStack.OPTIONAL_CODEC.orElse(ItemStack.EMPTY).sizeLimitedListOf(81).fieldOf("input").forGetter(PositionedProcessingPackageRecipeInfo::getMatrixAsList),
			MiscHelper.LARGE_ITEM_CODEC.orElse(ItemStack.EMPTY).sizeLimitedListOf(9).fieldOf("output").forGetter(IPackageRecipeInfo::getOutputs)).
			apply(instance, PositionedProcessingPackageRecipeInfo::new));
	public static final Codec<PositionedProcessingPackageRecipeInfo> CODEC = MAP_CODEC.codec();
	public static final StreamCodec<RegistryFriendlyByteBuf, PositionedProcessingPackageRecipeInfo> STREAM_CODEC = StreamCodec.composite(
			ItemStack.OPTIONAL_LIST_STREAM_CODEC, PositionedProcessingPackageRecipeInfo::getMatrixAsList,
			ItemStack.OPTIONAL_LIST_STREAM_CODEC, IPackageRecipeInfo::getOutputs,
			PositionedProcessingPackageRecipeInfo::new);

	private final List<ItemStack> input = new ArrayList<>();
	private final Int2ObjectMap<ItemStack> matrix = new Int2ObjectArrayMap<>(81);
	private final List<ItemStack> output;
	private final List<IPackagePattern> patterns = new ArrayList<>();

	public PositionedProcessingPackageRecipeInfo(List<ItemStack> inputs, List<ItemStack> outputs) {
		for(int i = 0; i < 81; ++i) {
			ItemStack stack = inputs.get(i).copy();
			if(!stack.isEmpty()) {
				matrix.put(i, stack);
				input.add(stack);
			}
		}
		output = MiscHelper.INSTANCE.condenseStacks(outputs, true);
		for(int i = 0; i*9 < this.input.size(); ++i) {
			patterns.add(new PackagePattern(this, i, true));
		}
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

	public List<ItemStack> getMatrixAsList() {
		List<ItemStack> matrixList = new ArrayList<>();
		for(int i = 0; i < 81; ++i) {
			matrixList.add(matrix.getOrDefault(i, ItemStack.EMPTY));
		}
		return matrixList;
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
		if(obj instanceof PositionedProcessingPackageRecipeInfo other) {
			return MiscHelper.INSTANCE.recipeEquals(this, null, other, null);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return MiscHelper.INSTANCE.recipeHashCode(this, null);
	}
}
