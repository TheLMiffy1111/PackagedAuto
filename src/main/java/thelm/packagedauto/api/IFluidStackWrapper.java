package thelm.packagedauto.api;

import net.minecraftforge.fluids.FluidStack;

public interface IFluidStackWrapper extends IVolumeStackWrapper {

	FluidStack getFluid();
}
