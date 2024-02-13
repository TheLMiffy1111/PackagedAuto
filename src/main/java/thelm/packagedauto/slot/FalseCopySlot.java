package thelm.packagedauto.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
	public boolean mayPickup(Player player) {
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
