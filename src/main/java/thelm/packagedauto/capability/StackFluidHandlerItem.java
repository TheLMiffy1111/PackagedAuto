package thelm.packagedauto.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class StackFluidHandlerItem implements IFluidHandlerItem {

	protected ItemStack container;

	public StackFluidHandlerItem(ItemStack container) {
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

	public void setFluid(FluidStack fluid)  {
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
		if(maxDrain < fluidStack.getAmount()) {
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
}
