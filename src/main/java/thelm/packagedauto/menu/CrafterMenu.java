package thelm.packagedauto.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.items.SlotItemHandler;
import thelm.packagedauto.block.entity.CrafterBlockEntity;
import thelm.packagedauto.menu.factory.PositionalBlockEntityMenuFactory;
import thelm.packagedauto.slot.CrafterRemoveOnlySlot;
import thelm.packagedauto.slot.RemoveOnlySlot;

public class CrafterMenu extends BaseMenu<CrafterBlockEntity> {

	public static final MenuType<CrafterMenu> TYPE_INSTANCE = (MenuType<CrafterMenu>)IForgeMenuType.
			create(new PositionalBlockEntityMenuFactory<>(CrafterMenu::new));

	public CrafterMenu(int windowId, Inventory inventory, CrafterBlockEntity blockEntity) {
		super(TYPE_INSTANCE, windowId, inventory, blockEntity);
		addSlot(new SlotItemHandler(itemHandler, 10, 8, 53));
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				addSlot(new CrafterRemoveOnlySlot(blockEntity, i*3+j, 44+j*18, 17+i*18));
			}
		}
		addSlot(new RemoveOnlySlot(itemHandler, 9, 134, 35));
		setupPlayerInventory();
	}
}
