package thelm.packagedauto.menu;

import net.minecraft.world.entity.player.Inventory;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.block.entity.BaseBlockEntity;
import thelm.packagedauto.inventory.BaseVolumeInventory;
import thelm.packagedauto.slot.PreviewVolumeSlot;

public class VolumeAmountSpecifyingMenu extends BaseMenu<BaseBlockEntity> {

	public VolumeAmountSpecifyingMenu(Inventory inventory, IVolumeStackWrapper stack) {
		super(null, 0, inventory, null);
		BaseVolumeInventory volumeInventory = new BaseVolumeInventory(stack.getVolumeType(), 1);
		volumeInventory.setStackInSlot(0, stack);
		addSlot(new PreviewVolumeSlot(volumeInventory, 0, 89, 48));
	}

	@Override
	public int getPlayerInvX() {
		return 0;
	}

	@Override
	public int getPlayerInvY() {
		return 0;
	}

	@Override
	public int getSizeInventory() {
		return 0;
	}
}
