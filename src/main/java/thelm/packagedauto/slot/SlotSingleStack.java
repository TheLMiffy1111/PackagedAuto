package thelm.packagedauto.slot;

import net.minecraft.inventory.IInventory;

public class SlotSingleStack extends SlotBase {

	public SlotSingleStack(IInventory inventory, int index, int xPosition, int yPosition) {
		super(inventory, index, xPosition, yPosition);
	}

	@Override
	public int getSlotStackLimit() {
		return 1;
	}
}
