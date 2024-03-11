package thelm.packagedauto.api;

import net.neoforged.neoforge.fluids.FluidStack;

public interface IFluidStackWrapper extends IVolumeStackWrapper {

	FluidStack getFluid();
}
