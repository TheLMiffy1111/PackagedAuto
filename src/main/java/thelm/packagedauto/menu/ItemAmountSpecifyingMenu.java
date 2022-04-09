package thelm.packagedauto.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import thelm.packagedauto.block.entity.BaseBlockEntity;
import thelm.packagedauto.slot.PreviewSlot;

public class ItemAmountSpecifyingMenu extends BaseMenu<BaseBlockEntity> {

	public ItemAmountSpecifyingMenu(Inventory inventory, ItemStack stack) {
		super(null, 0, inventory, null);
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
