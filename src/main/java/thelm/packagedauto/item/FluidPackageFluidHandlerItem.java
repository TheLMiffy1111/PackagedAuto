package thelm.packagedauto.item;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class FluidPackageFluidHandlerItem implements IFluidHandlerItem, ICapabilityProvider {

	private final LazyOptional<IFluidHandlerItem> holder;
	protected ItemStack container;

	public FluidPackageFluidHandlerItem(ItemStack container) {
		holder = LazyOptional.of(()->this);
		this.container = container;
	}

	@Override
	public ItemStack getContainer() {
		return container;
	}

	public FluidStack getFluid() {
		CompoundTag tagCompound = container.getTag();
		if(tagCompound == null || !tagCompound.contains("Fluid")) {
			return FluidStack.EMPTY;
		}
		return FluidStack.loadFluidStackFromNBT(tagCompound.getCompound("Fluid"));
	}

	protected void setFluid(FluidStack fluid)  {
		if(fluid != null && !fluid.isEmpty()) {
			if(!container.hasTag()) {
				container.setTag(new CompoundTag());
			}
			CompoundTag fluidTag = new CompoundTag();
			fluid.writeToNBT(fluidTag);
			container.getTag().put("Fluid", fluidTag);
		}
	}

	protected void setContainerToEmpty() {
		container.shrink(1);
	}

	@Override
	public int getTanks() {
		return 1;
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		return getFluid();
	}

	@Override
	public int getTankCapacity(int tank) {
		return getFluid().getAmount();
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return true;
	}

	@Override
	public int fill(FluidStack resource, FluidAction action)  {
		return 0;
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		FluidStack fluidStack = getFluid();
		if(resource.getAmount() < getFluid().getAmount()) {
			return FluidStack.EMPTY;
		}
		if(!fluidStack.isEmpty() && fluidStack.isFluidEqual(resource)) {
			if(action.execute()) {
				setContainerToEmpty();
			}
			return fluidStack;
		}
		return FluidStack.EMPTY;
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		FluidStack fluidStack = getFluid();
		if(container.getCount() != 1 || maxDrain < fluidStack.getAmount()) {
			return FluidStack.EMPTY;
		}
		if(!fluidStack.isEmpty()) {
			if(action.execute()) {
				setContainerToEmpty();
			}
			return fluidStack;
		}
		return FluidStack.EMPTY;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
		return CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.orEmpty(capability, holder);
	}

}
