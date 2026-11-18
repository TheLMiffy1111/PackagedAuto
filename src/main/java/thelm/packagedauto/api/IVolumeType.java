package thelm.packagedauto.api;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public interface IVolumeType {

	static final Codec<IVolumeType> CODEC = Identifier.CODEC.comapFlatMap(
			DataResult.partialGet(PackagedAutoApi.instance()::getVolumeType, ()->"Unknown volume type "), IVolumeType::getName);
	static final StreamCodec<ByteBuf, IVolumeType> STREAM_CODEC = Identifier.STREAM_CODEC.
			map(PackagedAutoApi.instance()::getVolumeType, IVolumeType::getName);

	Identifier getName();

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

	MapCodec<? extends IVolumeStackWrapper> getStackCodec();

	StreamCodec<RegistryFriendlyByteBuf, ? extends IVolumeStackWrapper> getStackStreamCodec();

	default void serializeStack(IVolumeStackWrapper stack, ValueOutput output) {
		MapCodec<IVolumeStackWrapper> codecCast = (MapCodec<IVolumeStackWrapper>)getStackCodec();
		output.store(codecCast, stack);
	}

	default IVolumeStackWrapper deserializeStack(ValueInput input) {
		MapCodec<IVolumeStackWrapper> codecCast = (MapCodec<IVolumeStackWrapper>)getStackCodec();
		return input.read(codecCast).orElse(getEmptyStackInstance());
	}

	Object makeItemCapability(ItemAccess itemAccess);

	ItemCapability<?, ItemAccess> getItemCapability();

	boolean hasBlockCapability(Level level, BlockPos pos, Direction direction);

	boolean isEmpty(Level level, BlockPos pos, Direction direction);

	int insert(Level level, BlockPos pos, Direction direction, IVolumeStackWrapper resource, TransactionContext transaction);

	int extract(Level level, BlockPos pos, Direction direction, IVolumeStackWrapper resource, TransactionContext transaction);

	void render(GuiGraphicsExtractor graphics, int i, int j, IVolumeStackWrapper stack);

	default int[] getIncrements() {
		return new int[] {100, 500, 1000};
	}

	default int[] getMultipliers() {
		return new int[] {2, 3, 5};
	}
}
