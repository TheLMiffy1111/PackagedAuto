package thelm.packagedauto.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

//Code from CoFHCore
public class FalseCopySlot extends SlotItemHandler {

	public int slotIndex;

	public FalseCopySlot(IItemHandler itemHandler, int index, int x, int y) {
		super(itemHandler, index, x, y);
		slotIndex = index;
	}

	@Override
	public boolean canTakeStack(PlayerEntity player) {
		return false;
	}

	@Override
	public void putStack(ItemStack stack) {
		if(!stack.isEmpty() && !isItemValid(stack)) {
			return;
		}
		super.putStack(stack);
	}
}