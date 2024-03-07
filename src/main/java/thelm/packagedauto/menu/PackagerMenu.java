package thelm.packagedauto.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.items.SlotItemHandler;
import thelm.packagedauto.block.entity.PackagerBlockEntity;
import thelm.packagedauto.menu.factory.PositionalBlockEntityMenuFactory;
import thelm.packagedauto.slot.RemoveOnlySlot;
import thelm.packagedauto.slot.SingleStackSlot;

public class PackagerMenu extends BaseMenu<PackagerBlockEntity> {

	public static final MenuType<PackagerMenu> TYPE_INSTANCE = IMenuTypeExtension.create(new PositionalBlockEntityMenuFactory<>(PackagerMenu::new));

	public PackagerMenu(int windowId, Inventory inventory, PackagerBlockEntity blockEntity) {
		super(TYPE_INSTANCE, windowId, inventory, blockEntity);
		addSlot(new SingleStackSlot(itemHandler, 10, 134, 17));
		addSlot(new SlotItemHandler(itemHandler, 11, 8, 53));
		for(int i = 0; i < 3; ++i)  {
			for(int j = 0; j < 3; ++j) {
				addSlot(new SlotItemHandler(itemHandler, j+i*3, 44+j*18, 17+i*18));
			}
		}
		addSlot(new RemoveOnlySlot(itemHandler, 9, 134, 53));
		setupPlayerInventory();
	}
}
