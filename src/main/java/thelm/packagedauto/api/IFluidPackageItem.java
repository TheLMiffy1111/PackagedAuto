package thelm.packagedauto.api;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface IFluidPackageItem {

	FluidStack getFluidStack(ItemStack stack);
}
