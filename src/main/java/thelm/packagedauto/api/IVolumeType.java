package thelm.packagedauto.api;

import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

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
	Capability getItemCapability();

	boolean hasBlockCapability(ICapabilityProvider capProvider, Direction direction);

	default boolean isEmpty(ICapabilityProvider capProvider, Direction direction) {
		return false;
	}

	int fill(ICapabilityProvider capProvider, Direction direction, IVolumeStackWrapper resource, boolean simulate);

	IVolumeStackWrapper drain(ICapabilityProvider capProvider, Direction direction, IVolumeStackWrapper resource, boolean simulate);

	void render(PoseStack poseStack, int i, int j, IVolumeStackWrapper stack);
}
