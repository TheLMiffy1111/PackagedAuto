package thelm.packagedauto.api;

import net.minecraft.world.item.ItemStack;

public interface IMarkerItem {

	DirectionalGlobalPos getDirectionalGlobalPos(ItemStack stack);

	void setDirectionalGlobalPos(ItemStack stack, DirectionalGlobalPos pos);
}
