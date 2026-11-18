package thelm.packagedauto.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class PreviewSlot extends ResourceHandlerSlot {

	public final ResourceHandler<ItemResource> itemHandler;

	public PreviewSlot(ResourceHandler<ItemResource> itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, null, index, xPosition, yPosition);
		this.itemHandler = itemHandler;
	}

	@Override
	public boolean mayPickup(Player player) {
		return false;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return false;
	}

	@Override
	protected void setStackCopy(ItemStack stack) {}
}
