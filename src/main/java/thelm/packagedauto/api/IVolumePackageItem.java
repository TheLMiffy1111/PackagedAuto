package thelm.packagedauto.api;

import net.minecraft.world.item.ItemStack;

public interface IVolumePackageItem {

	IVolumeType getVolumeType(ItemStack stack);

	IVolumeStackWrapper getVolumeStack(ItemStack stack);
}
