package thelm.packagedauto.menu;

import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import thelm.packagedauto.block.entity.PackagerExtensionBlockEntity;
import thelm.packagedauto.slot.PreviewStackSlot;
import thelm.packagedauto.slot.RemoveOnlySlot;

public class PackagerExtensionMenu extends BaseMenu<PackagerExtensionBlockEntity> {

	public PackagerExtensionMenu(int windowId, Inventory inventory, PackagerExtensionBlockEntity blockEntity) {
		super(PackagedAutoMenus.PACKAGER_EXTENSION.get(), windowId, inventory, blockEntity);
		addSlot(new ResourceHandlerSlot(itemHandler, itemHandler, 10, 8, 53));
		for(int i = 0; i < 3; ++i)  {
			for(int j = 0; j < 3; ++j) {
				addSlot(new ResourceHandlerSlot(itemHandler, itemHandler, j+i*3, 44+j*18, 17+i*18));
			}
		}
		addSlot(new RemoveOnlySlot(itemHandler, itemHandler, 9, 134, 53));
		addSlot(new PreviewStackSlot(()->blockEntity.listStack, 134, 17));
		setupPlayerInventory();
	}
}
