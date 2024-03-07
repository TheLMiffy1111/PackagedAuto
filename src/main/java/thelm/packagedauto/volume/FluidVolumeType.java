package thelm.packagedauto.volume;

import java.util.Optional;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
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
	public ItemCapability<IFluidHandlerItem, Void> getItemCapability() {
		return Capabilities.FluidHandler.ITEM;
	}

	@Override
	public boolean hasBlockCapability(Level level, BlockPos pos, Direction direction) {
		return level.getCapability(Capabilities.FluidHandler.BLOCK, pos, direction) != null;
	}

	@Override
	public boolean isEmpty(Level level, BlockPos pos, Direction direction) {
		IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, direction);
		if(handler != null) {
			if(handler.getTanks() == 0) {
				return false;
			}
			for(int i = 0; i < handler.getTanks(); ++i) {
				if(!handler.getFluidInTank(i).isEmpty()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public int fill(Level level, BlockPos pos, Direction direction, IVolumeStackWrapper resource, boolean simulate) {
		if(resource instanceof FluidStackWrapper fluidStack) {
			IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, direction);
			if(handler != null) {
				FluidAction action = simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE;
				return handler.fill(fluidStack.stack(), action);
			}
		}
		return 0;
	}

	@Override
	public IVolumeStackWrapper drain(Level level, BlockPos pos, Direction direction, IVolumeStackWrapper resource, boolean simulate) {
		if(resource instanceof FluidStackWrapper fluidStack) {
			IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, direction);
			if(handler != null) {
				FluidAction action = simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE;
				return new FluidStackWrapper(handler.drain(fluidStack.stack(), action));
			}
		}
		return FluidStackWrapper.EMPTY;
	}

	@Override
	public void render(GuiGraphics graphics, int i, int j, IVolumeStackWrapper stack) {
		if(stack instanceof FluidStackWrapper fluidStack) {
			FluidRenderer.INSTANCE.render(graphics, i, j, fluidStack.stack());
		}
	}
}
