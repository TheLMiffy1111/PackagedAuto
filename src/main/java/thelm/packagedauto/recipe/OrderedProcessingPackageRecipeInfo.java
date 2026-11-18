package thelm.packagedauto.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.util.MiscHelper;
import thelm.packagedauto.util.PackagePattern;

public class OrderedProcessingPackageRecipeInfo implements IPackageRecipeInfo {

	public static final MapCodec<OrderedProcessingPackageRecipeInfo> MAP_CODEC = RecordCodecBuilder.mapCodec(instance->instance.group(
			ItemStack.CODEC.orElse(ItemStack.EMPTY).sizeLimitedListOf(81).fieldOf("input").forGetter(IPackageRecipeInfo::getInputs),
			MiscHelper.LARGE_ITEM_CODEC.orElse(ItemStack.EMPTY).sizeLimitedListOf(9).fieldOf("output").forGetter(IPackageRecipeInfo::getOutputs)).
			apply(instance, OrderedProcessingPackageRecipeInfo::new));
	public static final Codec<OrderedProcessingPackageRecipeInfo> CODEC = MAP_CODEC.codec();
	public static final StreamCodec<RegistryFriendlyByteBuf, OrderedProcessingPackageRecipeInfo> STREAM_CODEC = StreamCodec.composite(
			ItemStack.OPTIONAL_LIST_STREAM_CODEC, IPackageRecipeInfo::getInputs,
			ItemStack.OPTIONAL_LIST_STREAM_CODEC, IPackageRecipeInfo::getOutputs,
			OrderedProcessingPackageRecipeInfo::new);

	private final List<ItemStack> input;
	private final List<ItemStack> output;
	private final List<IPackagePattern> patterns = new ArrayList<>();

	public OrderedProcessingPackageRecipeInfo(List<ItemStack> inputs, List<ItemStack> outputs) {
		input = removeEmptyStacks(inputs);
		output = MiscHelper.INSTANCE.condenseStacks(outputs, true);
		for(int i = 0; i*9 < input.size(); ++i) {
			patterns.add(new PackagePattern(this, i, true));
		}
	}

	private static List<ItemStack> removeEmptyStacks(List<ItemStack> stacks) {
		return stacks.stream().filter(stack -> !stack.isEmpty()).toList();
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

	@Override
	public Int2ObjectMap<ItemStack> getEncoderStacks() {
		Int2ObjectMap<ItemStack> map = new Int2ObjectArrayMap<>();
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
			return MiscHelper.INSTANCE.recipeEquals(this, null, other, null);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return MiscHelper.INSTANCE.recipeHashCode(this, null);
	}
}
