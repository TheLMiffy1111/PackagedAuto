package thelm.packagedauto.slot;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

//Code from CoFHCore
public class RemoveOnlySlot extends SlotItemHandler {

	public RemoveOnlySlot(IItemHandler itemHandler, int index, int x, int y) {
		super(itemHandler, index, x, y);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return false;
	}
}