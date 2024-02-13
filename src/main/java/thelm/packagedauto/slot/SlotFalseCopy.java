package thelm.packagedauto.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

//Code from CoFHCore
public class SlotFalseCopy extends SlotBase {

	public int slotIndex;

	public SlotFalseCopy(IInventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
		slotIndex = index;
	}

	@Override
	public boolean canTakeStack(EntityPlayer player) {
		return false;
	}

	@Override
	public void putStack(ItemStack stack) {
		if(!isItemValid(stack)) {
			return;
		}
		inventory.setInventorySlotContents(slotIndex, stack);
		onSlotChanged();
	}
}
