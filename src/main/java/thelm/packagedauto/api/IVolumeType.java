package thelm.packagedauto.api;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.ItemCapability;

public interface IVolumeType {

	static final Codec<IVolumeType> CODEC = ResourceLocation.CODEC.comapFlatMap(
			DataResult.partialGet(PackagedAutoApi.instance()::getVolumeType, ()->"Unknown volume type "), IVolumeType::getName);
	static final StreamCodec<ByteBuf, IVolumeType> STREAM_CODEC = ResourceLocation.STREAM_CODEC.
			map(PackagedAutoApi.instance()::getVolumeType, IVolumeType::getName);

	ResourceLocation getName();

	Class<?> getTypeClass();

	default Class<?> getTypeBaseClass() {
		return getTypeClass();
	}

	MutableComponent getDisplayName();

	default boolean supportsAE() {
		return false;
	}

	default Optional<?> makeStackFromBase(Object volumeBase, int amount, DataComponentPatch patch) {
		return Optional.empty();
	}

	IVolumeStackWrapper getEmptyStackInstance();

	Optional<IVolumeStackWrapper> wrapStack(Object volumeStack);

	Optional<IVolumeStackWrapper> getStackContained(ItemStack container);

	void setStack(ItemStack stack, IVolumeStackWrapper volumeStack);

	Codec<? extends IVolumeStackWrapper> getStackCodec();

	StreamCodec<RegistryFriendlyByteBuf, ? extends IVolumeStackWrapper> getStackStreamCodec();

	default CompoundTag saveRawStack(CompoundTag nbt, IVolumeStackWrapper stack, HolderLookup.Provider registries) {
		Codec<IVolumeStackWrapper> codecCast = (Codec<IVolumeStackWrapper>)getStackCodec();
		if(!(codecCast instanceof MapCodec.MapCodecCodec)) {
			codecCast = codecCast.fieldOf("stack").codec();
		}
		return nbt.merge((CompoundTag)codecCast.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack).result().orElse(new CompoundTag()));
	}

	default IVolumeStackWrapper loadRawStack(CompoundTag nbt, HolderLookup.Provider registries) {
		Codec<IVolumeStackWrapper> codecCast = (Codec<IVolumeStackWrapper>)getStackCodec();
		return codecCast.parse(registries.createSerializationContext(NbtOps.INSTANCE), nbt).result().orElse(getEmptyStackInstance());
	}

	Object makeItemCapability(ItemStack volumePackage);

	@SuppressWarnings("rawtypes")
	ItemCapability getItemCapability();

	boolean hasBlockCapability(Level level, BlockPos pos, Direction direction);

	boolean isEmpty(Level level, BlockPos pos, Direction direction);

	int fill(Level level, BlockPos pos, Direction direction, IVolumeStackWrapper resource, boolean simulate);

	IVolumeStackWrapper drain(Level level, BlockPos pos, Direction direction, IVolumeStackWrapper resource, boolean simulate);

	void render(GuiGraphics graphics, int i, int j, IVolumeStackWrapper stack);

	default int[] getIncrements() {
		return new int[] {100, 500, 1000};
	}

	default int[] getMultipliers() {
		return new int[] {2, 3, 5};
	}
}
