package thelm.packagedauto.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import thelm.packagedauto.block.entity.CraftingProxyBlockEntity;
import thelm.packagedauto.menu.factory.PositionalBlockEntityMenuFactory;
import thelm.packagedauto.slot.SingleStackSlot;

public class CraftingProxyMenu extends BaseMenu<CraftingProxyBlockEntity> {

	public static final MenuType<CraftingProxyMenu> TYPE_INSTANCE = (MenuType<CraftingProxyMenu>)IForgeMenuType.
			create(new PositionalBlockEntityMenuFactory<>(CraftingProxyMenu::new)).
			setRegistryName("packagedauto:crafting_proxy");

	public CraftingProxyMenu(int windowId, Inventory inventory, CraftingProxyBlockEntity blockEntity) {
		super(TYPE_INSTANCE, windowId, inventory, blockEntity);
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
