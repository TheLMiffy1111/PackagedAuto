package thelm.packagedauto.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import thelm.packagedauto.slot.PreviewSlot;
import thelm.packagedauto.tile.BaseTile;

public class AmountSpecifyingContainer extends BaseContainer<BaseTile> {

	public AmountSpecifyingContainer(PlayerInventory playerInventory, ItemStack stack) {
		super(null, 0, playerInventory, null);
		ItemStackHandler itemInventory = new ItemStackHandler(1);
		itemInventory.setStackInSlot(0, ItemHandlerHelper.copyStackWithSize(stack, 1));
		addSlot(new PreviewSlot(itemInventory, 0, 89, 48));
	}

	@Override
	public int getPlayerInvX() {
		return 0;
	}

	@Override
	public int getPlayerInvY() {
		return 0;
	}

	@Override
	public int getSizeInventory() {
		return 0;
	}
}
