package thelm.packagedauto.api;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IPackageRecipeType {

	static final Codec<IPackageRecipeType> CODEC = ResourceLocation.CODEC.comapFlatMap(
			DataResult.partialGet(PackagedAutoApi.instance()::getRecipeType, ()->"Unknown recipe type "), IPackageRecipeType::getName).
			orElse(PackagedAutoApi.instance().getRecipeType(ResourceLocation.parse("packagedauto:ordered_processing")));
	static final StreamCodec<ByteBuf, IPackageRecipeType> STREAM_CODEC = ResourceLocation.STREAM_CODEC.
			map(PackagedAutoApi.instance()::getRecipeType, IPackageRecipeType::getName);

	ResourceLocation getName();

	MutableComponent getDisplayName();

	MutableComponent getShortDisplayName();

	MapCodec<? extends IPackageRecipeInfo> getRecipeInfoMapCodec();

	Codec<? extends IPackageRecipeInfo> getRecipeInfoCodec();

	StreamCodec<RegistryFriendlyByteBuf, ? extends IPackageRecipeInfo> getRecipeInfoStreamCodec();

	IPackageRecipeInfo generateRecipeInfoFromStacks(List<ItemStack> inputs, List<ItemStack> outputs, Level level);

	IntSet getEnabledSlots();

	default boolean canSetOutput() {
		return false;
	}

	default boolean hasMachine() {
		return true;
	}

	default boolean isOrdered() {
		return false;
	}

	default boolean hasCraftingRemainingItem() {
		return true;
	}

	default List<ResourceLocation> getJEICategories() {
		return List.of();
	}

	default List<ResourceLocation> getEMICategories() {
		return getJEICategories();
	}

	default Int2ObjectMap<ItemStack> getRecipeTransferMap(IRecipeSlotsViewWrapper recipeLayoutWrapper) {
		return Int2ObjectMaps.emptyMap();
	}

	Object getRepresentation();

	Vec3i getSlotColor(int slot);
}
