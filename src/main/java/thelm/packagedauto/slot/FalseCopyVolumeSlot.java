package thelm.packagedauto.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.world.inventory.StackCopySlot;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.api.IVolumeType;
import thelm.packagedauto.inventory.BaseVolumeInventory;

public class FalseCopyVolumeSlot extends StackCopySlot {

	public BaseVolumeInventory volumeInventory;

	public FalseCopyVolumeSlot(BaseVolumeInventory volumeInventory, int index, int x, int y) {
		super(index, x, y);
		this.volumeInventory = volumeInventory;
	}

	@Override
	public boolean mayPickup(Player player) {
		return false;
	}

	@Override
	protected ItemStack getStackCopy() {
		return ItemStack.EMPTY;
	}

	@Override
	protected void setStackCopy(ItemStack stack) {
		IVolumeType type = volumeInventory.type;
		if(stack.isEmpty()) {
			volumeInventory.setStack(getSlotIndex(), type.getEmptyStackInstance());
		}
		else {
			IVolumeStackWrapper vStack = type.getStackContained(stack).orElse(type.getEmptyStackInstance());
			if(!vStack.isEmpty()) {
				volumeInventory.setStack(getSlotIndex(), vStack.copy());
			}
		}
	}
}
