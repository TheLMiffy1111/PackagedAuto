package thelm.packagedauto.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

//Code from CoFHCore
public class FalseCopySlot extends SlotItemHandler {

	public int slotIndex;

	public FalseCopySlot(IItemHandler itemHandler, int index, int x, int y) {
		super(itemHandler, index, x, y);
		slotIndex = index;
	}

	@Override
	public boolean mayPickup(PlayerEntity player) {
		return false;
	}

	@Override
	public void set(ItemStack stack) {
		if(!stack.isEmpty() && !mayPlace(stack)) {
			return;
		}
		super.set(stack);
	}
}
