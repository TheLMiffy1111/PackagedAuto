package thelm.packagedauto.menu;

import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import thelm.packagedauto.block.entity.PackagerBlockEntity;
import thelm.packagedauto.slot.RemoveOnlySlot;
import thelm.packagedauto.slot.SingleStackSlot;

public class PackagerMenu extends BaseMenu<PackagerBlockEntity> {

	public PackagerMenu(int windowId, Inventory inventory, PackagerBlockEntity blockEntity) {
		super(PackagedAutoMenus.PACKAGER.get(), windowId, inventory, blockEntity);
		addSlot(new SingleStackSlot(itemHandler, itemHandler, 10, 134, 17));
		addSlot(new ResourceHandlerSlot(itemHandler, itemHandler, 11, 8, 53));
		for(int i = 0; i < 3; ++i)  {
			for(int j = 0; j < 3; ++j) {
				addSlot(new ResourceHandlerSlot(itemHandler, itemHandler, j+i*3, 44+j*18, 17+i*18));
			}
		}
		addSlot(new RemoveOnlySlot(itemHandler, itemHandler, 9, 134, 53));
		setupPlayerInventory();
	}
}
