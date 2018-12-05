package thelm.packagedauto.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

//Code from CoFHCore
public class SlotRemoveOnly extends SlotBase {

	public SlotRemoveOnly(IInventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return false;
	}
}