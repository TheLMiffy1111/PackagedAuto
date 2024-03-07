package thelm.packagedauto.api;

import java.util.Optional;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.ItemCapability;

public interface IVolumeType {

	ResourceLocation getName();

	Class<?> getTypeClass();

	MutableComponent getDisplayName();

	default boolean supportsAE() {
		return false;
	}

	IVolumeStackWrapper getEmptyStackInstance();

	Optional<IVolumeStackWrapper> wrapStack(Object volumeStack);

	Optional<IVolumeStackWrapper> getStackContained(ItemStack container);

	void setStack(ItemStack stack, IVolumeStackWrapper volumeStack);

	IVolumeStackWrapper loadStack(CompoundTag tag);

	Object makeItemCapability(ItemStack volumePackage);

	@SuppressWarnings("rawtypes")
	ItemCapability getItemCapability();

	boolean hasBlockCapability(Level level, BlockPos pos, Direction direction);

	boolean isEmpty(Level level, BlockPos pos, Direction direction);

	int fill(Level level, BlockPos pos, Direction direction, IVolumeStackWrapper resource, boolean simulate);

	IVolumeStackWrapper drain(Level level, BlockPos pos, Direction direction, IVolumeStackWrapper resource, boolean simulate);

	void render(GuiGraphics graphics, int i, int j, IVolumeStackWrapper stack);
}
