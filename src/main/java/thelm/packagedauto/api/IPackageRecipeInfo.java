package thelm.packagedauto.api;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

/**
 * Please override {@link IPackageRecipeInfo#equals(IPackageRecipeInfo)} when implementing a new recipe type.
 */
public interface IPackageRecipeInfo {

	static final MapCodec<IPackageRecipeInfo> MAP_CODEC = IPackageRecipeType.CODEC.dispatchMap(
			"recipe_type", IPackageRecipeInfo::getRecipeType, IPackageRecipeType::getRecipeInfoMapCodec);
	static final Codec<IPackageRecipeInfo> CODEC = Codec.lazyInitialized(MAP_CODEC::codec);
	static final StreamCodec<RegistryFriendlyByteBuf, IPackageRecipeInfo> STREAM_CODEC = IPackageRecipeType.STREAM_CODEC.
			<RegistryFriendlyByteBuf>cast().dispatch(IPackageRecipeInfo::getRecipeType, IPackageRecipeType::getRecipeInfoStreamCodec);

	IPackageRecipeType getRecipeType();

	boolean isValid();

	List<IPackagePattern> getPatterns();

	default List<IPackagePattern> getExtraPatterns() {
		return List.of();
	}

	List<ItemStack> getInputs();

	List<ItemStack> getOutputs();

	Int2ObjectMap<ItemStack> getEncoderStacks();

	default ItemStack getCraftingRemainingItem(ItemStack stack) {
		if(getRecipeType().hasCraftingRemainingItem()) {
			ItemStackTemplate remainder = stack.getCraftingRemainder();
			if(remainder != null) {
				return remainder.create();
			}
		}
		return ItemStack.EMPTY;
	}

	default boolean validPatternIndex(int index) {
		return index >= 0 && index < getPatterns().size();
	}

	default boolean isPackageable() {
		return isValid() && !getInputs().isEmpty();
	}

	default boolean isCraftable() {
		return isPackageable() && !getOutputs().isEmpty();
	}

	@Override
	boolean equals(Object obj);

	@Override
	int hashCode();
}
