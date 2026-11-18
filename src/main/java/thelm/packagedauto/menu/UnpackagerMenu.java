package thelm.packagedauto.menu;

import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity;
import thelm.packagedauto.slot.SingleStackSlot;

public class UnpackagerMenu extends BaseMenu<UnpackagerBlockEntity> {

	public UnpackagerMenu(int windowId, Inventory inventory, UnpackagerBlockEntity blockEntity) {
		super(PackagedAutoMenus.UNPACKAGER.get(), windowId, inventory, blockEntity);
		addSlot(new SingleStackSlot(itemHandler, itemHandler, 9, 26, 17));
		addSlot(new ResourceHandlerSlot(itemHandler, itemHandler, 10, 8, 53));
		for(int i = 0; i < 3; ++i)  {
			for(int j = 0; j < 3; ++j) {
				addSlot(new ResourceHandlerSlot(itemHandler, itemHandler, j+i*3, 44+j*18, 17+i*18));
			}
		}
		setupPlayerInventory();
	}
}
