package thelm.packagedauto.volume;

import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.api.IVolumeType;
import thelm.packagedauto.capability.StackFluidHandlerItem;
import thelm.packagedauto.client.FluidRenderer;

public class FluidVolumeType implements IVolumeType {

	public static final FluidVolumeType INSTANCE = new FluidVolumeType();
	public static final ResourceLocation NAME = new ResourceLocation("minecraft:fluid");

	@Override
	public ResourceLocation getName() {
		return NAME;
	}

	@Override
	public Class<?> getTypeClass() {
		return FluidStack.class;
	}

	@Override
	public MutableComponent getDisplayName() {
		return Component.translatable("volume.packagedauto.minecraft.fluid");
	}

	@Override
	public boolean supportsAE() {
		return true;
	}

	@Override
	public IVolumeStackWrapper getEmptyStackInstance() {
		return FluidStackWrapper.EMPTY;
	}

	@Override
	public Optional<IVolumeStackWrapper> wrapStack(Object volumeStack) {
		if(volumeStack instanceof FluidStack fluidStack) {
			return Optional.of(new FluidStackWrapper(fluidStack));
		}
		return Optional.empty();
	}

	@Override
	public Optional<IVolumeStackWrapper> getStackContained(ItemStack container) {
		return FluidUtil.getFluidContained(container).map(FluidStackWrapper::new);
	}

	@Override
	public void setStack(ItemStack stack, IVolumeStackWrapper volumeStack) {
		if(volumeStack instanceof FluidStackWrapper fluidStack) {
			FluidUtil.getFluidHandler(stack).ifPresent(handler->{
				if(handler instanceof StackFluidHandlerItem vHandler) {
					vHandler.setFluid(fluidStack.stack());
				}
			});
		}
	}

	@Override
	public IVolumeStackWrapper loadStack(CompoundTag tag) {
		return new FluidStackWrapper(FluidStack.loadFluidStackFromNBT(tag));
	}

	@Override
	public IFluidHandlerItem makeItemCapability(ItemStack volumePackage) {
		return new StackFluidHandlerItem(volumePackage);
	}

	@Override
	public Capability<IFluidHandlerItem> getItemCapability() {
		return ForgeCapabilities.FLUID_HANDLER_ITEM;
	}

	@Override
	public boolean hasBlockCapability(ICapabilityProvider capProvider, Direction direction) {
		return capProvider.getCapability(ForgeCapabilities.FLUID_HANDLER, direction).isPresent();
	}

	@Override
	public boolean isEmpty(ICapabilityProvider capProvider, Direction direction) {
		return capProvider.getCapability(ForgeCapabilities.FLUID_HANDLER, direction).map(handler->{
			if(handler.getTanks() == 0) {
				return false;
			}
			for(int i = 0; i < handler.getTanks(); ++i) {
				if(!handler.getFluidInTank(i).isEmpty()) {
					return false;
				}
			}
			return true;
		}).orElse(false);
	}

	@Override
	public int fill(ICapabilityProvider capProvider, Direction direction, IVolumeStackWrapper resource, boolean simulate) {
		if(resource instanceof FluidStackWrapper fluidStack) {
			FluidAction action = simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE;
			return capProvider.getCapability(ForgeCapabilities.FLUID_HANDLER, direction).
					map(handler->handler.fill(fluidStack.stack(), action)).orElse(0);
		}
		return 0;
	}

	@Override
	public IVolumeStackWrapper drain(ICapabilityProvider capProvider, Direction direction, IVolumeStackWrapper resource, boolean simulate) {
		if(resource instanceof FluidStackWrapper fluidStack) {
			FluidAction action = simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE;
			return capProvider.getCapability(ForgeCapabilities.FLUID_HANDLER, direction).
					map(handler->handler.drain(fluidStack.stack(), action)).
					map(FluidStackWrapper::new).orElse(FluidStackWrapper.EMPTY);
		}
		return FluidStackWrapper.EMPTY;
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, IVolumeStackWrapper stack) {
		if(stack instanceof FluidStackWrapper fluidStack) {
			FluidRenderer.INSTANCE.render(poseStack, i, j, fluidStack.stack());
		}
	}
}
