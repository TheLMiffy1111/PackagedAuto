package thelm.packagedauto.capability;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import thelm.packagedauto.api.IFluidStackWrapper;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.volume.FluidStackWrapper;

public class VolumeFluidResourceHandler extends ItemAccessResourceHandler<FluidResource> {

	public VolumeFluidResourceHandler(ItemAccess itemAccess) {
		super(itemAccess, 1);
	}

	public FluidStack getFluid() {
		IVolumeStackWrapper stack = itemAccess.getResource().get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK);
		if(stack instanceof IFluidStackWrapper fluid) {
			return fluid.getFluid();
		}
		return FluidStack.EMPTY;
	}

	public void setFluid(FluidStack fluid)  {
		if(fluid != null && !fluid.isEmpty()) {
			ItemResource newResource = itemAccess.getResource().with(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK, FluidStackWrapper.of(fluid));
			itemAccess.exchange(newResource, itemAccess.getAmount(), null);
		}
	}

	@Override
	protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
		return FluidResource.of(getFluid());
	}

	@Override
	protected int getAmountFrom(ItemResource accessResource, int index) {
		return getFluid().amount();
	}

	@Override
	protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
		if(newAmount == 0) {
			return null;
		}
		else {
			return ItemResource.EMPTY;
		}
	}

	@Override
	protected int getCapacity(int index, FluidResource resource) {
		return getFluid().amount();
	}
}
