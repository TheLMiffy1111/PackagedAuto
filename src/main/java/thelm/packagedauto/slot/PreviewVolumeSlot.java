package thelm.packagedauto.slot;

import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.inventory.BaseVolumeInventory;

public class PreviewVolumeSlot extends FalseCopyVolumeSlot {

	public PreviewVolumeSlot(BaseVolumeInventory fluidInventory, int index, int x, int y) {
		super(fluidInventory, index, x, y);
	}

	@Override
	public void setStackCopy(ItemStack stack) {}
}
