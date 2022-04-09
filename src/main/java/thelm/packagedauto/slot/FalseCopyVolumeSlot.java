package thelm.packagedauto.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.api.IVolumeType;
import thelm.packagedauto.inventory.BaseVolumeInventory;

public class FalseCopyVolumeSlot extends SlotItemHandler {

	public int slotIndex;
	public BaseVolumeInventory volumeInventory;

	public FalseCopyVolumeSlot(BaseVolumeInventory volumeInventory, int index, int x, int y) {
		super(new ItemStackHandler(volumeInventory.getSlots()), index, x, y);
		slotIndex = index;
		this.volumeInventory = volumeInventory;
	}

	@Override
	public boolean mayPickup(Player player) {
		return false;
	}

	@Override
	public void set(ItemStack stack) {
		IVolumeType type = volumeInventory.type;
		if(stack.isEmpty()) {
			volumeInventory.setStackInSlot(getSlotIndex(), type.getEmptyStackInstance());
		}
		else {
			IVolumeStackWrapper vStack = type.getStackContained(stack).orElse(type.getEmptyStackInstance());
			if(!vStack.isEmpty()) {
				volumeInventory.setStackInSlot(getSlotIndex(), vStack);
			}
		}
	}
}
