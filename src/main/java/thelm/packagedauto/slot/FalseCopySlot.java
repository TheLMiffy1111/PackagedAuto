package thelm.packagedauto.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

//Code from CoFHCore
public class FalseCopySlot extends ResourceHandlerSlot {

	public IndexModifier<ItemResource> slotModifier;
	public int slotIndex;

	public FalseCopySlot(ResourceHandler<ItemResource> itemHandler, IndexModifier<ItemResource> slotModifier, int index, int x, int y) {
		super(itemHandler, slotModifier, index, x, y);
		this.slotModifier = slotModifier;
		slotIndex = index;
	}

	@Override
	public boolean mayPickup(Player player) {
		return false;
	}

	@Override
	protected void setStackCopy(ItemStack stack) {
		if(!stack.isEmpty() && !mayPlace(stack)) {
			return;
		}
		super.setStackCopy(stack);
	}
}
