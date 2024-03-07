package thelm.packagedauto.slot;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

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
