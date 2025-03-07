package thelm.packagedauto.menu;

import net.minecraft.world.entity.player.Inventory;
import thelm.packagedauto.block.entity.CraftingProxyBlockEntity;
import thelm.packagedauto.slot.SingleStackSlot;

public class CraftingProxyMenu extends BaseMenu<CraftingProxyBlockEntity> {

	public CraftingProxyMenu(int windowId, Inventory inventory, CraftingProxyBlockEntity blockEntity) {
		super(PackagedAutoMenus.CRAFTING_PROXY.get(), windowId, inventory, blockEntity);
		addSlot(new SingleStackSlot(itemHandler, 0, 80, 17));
		setupPlayerInventory();
	}

	@Override
	public int getPlayerInvX() {
		return 8;
	}

	@Override
	public int getPlayerInvY() {
		return 48;
	}
}
