package thelm.packagedauto.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.items.SlotItemHandler;
import thelm.packagedauto.block.entity.PackagerExtensionBlockEntity;
import thelm.packagedauto.menu.factory.PositionalBlockEntityMenuFactory;
import thelm.packagedauto.slot.PreviewSlot;
import thelm.packagedauto.slot.RemoveOnlySlot;

public class PackagerExtensionMenu extends BaseMenu<PackagerExtensionBlockEntity> {

	public static final MenuType<PackagerExtensionMenu> TYPE_INSTANCE = IMenuTypeExtension.create(new PositionalBlockEntityMenuFactory<>(PackagerExtensionMenu::new));

	public PackagerExtensionMenu(int windowId, Inventory inventory, PackagerExtensionBlockEntity blockEntity) {
		super(TYPE_INSTANCE, windowId, inventory, blockEntity);
		addSlot(new SlotItemHandler(itemHandler, 10, 8, 53));
		for(int i = 0; i < 3; ++i)  {
			for(int j = 0; j < 3; ++j) {
				addSlot(new SlotItemHandler(itemHandler, j+i*3, 44+j*18, 17+i*18));
			}
		}
		addSlot(new RemoveOnlySlot(itemHandler, 9, 134, 53));
		addSlot(new PreviewSlot(blockEntity.listStackItemHandler, 0, 134, 17));
		setupPlayerInventory();
	}
}
