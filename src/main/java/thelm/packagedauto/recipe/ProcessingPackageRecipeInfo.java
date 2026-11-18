package thelm.packagedauto.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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

public class ProcessingPackageRecipeInfo implements IPackageRecipeInfo {

	public static final MapCodec<ProcessingPackageRecipeInfo> MAP_CODEC = RecordCodecBuilder.mapCodec(instance->instance.group(
			ItemStack.CODEC.orElse(ItemStack.EMPTY).sizeLimitedListOf(81).fieldOf("input").forGetter(IPackageRecipeInfo::getInputs),
			MiscHelper.LARGE_ITEM_CODEC.orElse(ItemStack.EMPTY).sizeLimitedListOf(9).fieldOf("output").forGetter(IPackageRecipeInfo::getOutputs)).
			apply(instance, ProcessingPackageRecipeInfo::new));
	public static final Codec<ProcessingPackageRecipeInfo> CODEC = MAP_CODEC.codec();
	public static final StreamCodec<RegistryFriendlyByteBuf, ProcessingPackageRecipeInfo> STREAM_CODEC = StreamCodec.composite(
			ItemStack.OPTIONAL_LIST_STREAM_CODEC, IPackageRecipeInfo::getInputs,
			ItemStack.OPTIONAL_LIST_STREAM_CODEC, IPackageRecipeInfo::getOutputs,
			ProcessingPackageRecipeInfo::new);

	private final List<ItemStack> input;
	private final List<ItemStack> output;
	private final List<IPackagePattern> patterns = new ArrayList<>();

	public ProcessingPackageRecipeInfo(List<ItemStack> inputs, List<ItemStack> outputs) {
		input = MiscHelper.INSTANCE.condenseStacks(inputs);
		output = MiscHelper.INSTANCE.condenseStacks(outputs, true);
		for(int i = 0; i*9 < input.size(); ++i) {
			patterns.add(new PackagePattern(this, i, true));
		}
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
		if(obj instanceof ProcessingPackageRecipeInfo other) {
			return MiscHelper.INSTANCE.recipeEquals(this, null, other, null);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return MiscHelper.INSTANCE.recipeHashCode(this, null);
	}
}
