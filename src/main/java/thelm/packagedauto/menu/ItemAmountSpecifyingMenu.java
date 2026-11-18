package thelm.packagedauto.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.block.entity.BaseBlockEntity;
import thelm.packagedauto.slot.PreviewStackSlot;

public class ItemAmountSpecifyingMenu extends BaseMenu<BaseBlockEntity> {

	public ItemAmountSpecifyingMenu(Inventory inventory, ItemStack stack) {
		super(null, 0, inventory, null);
		addSlot(new PreviewStackSlot(()->stack, 89, 48));
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
