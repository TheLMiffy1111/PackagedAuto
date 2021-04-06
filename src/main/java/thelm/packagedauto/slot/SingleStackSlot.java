package thelm.packagedauto.slot;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SingleStackSlot extends SlotItemHandler {

	public SingleStackSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, index, xPosition, yPosition);
	}

	@Override
	public int getSlotStackLimit() {
		return 1;
	}
}
