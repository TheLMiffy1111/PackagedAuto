package thelm.packagedauto.menu;

import net.minecraft.world.entity.player.Inventory;
import thelm.packagedauto.block.entity.PackagingProviderBlockEntity;
import thelm.packagedauto.slot.SingleStackSlot;

public class PackagingProviderMenu extends BaseMenu<PackagingProviderBlockEntity> {

	public PackagingProviderMenu(int windowId, Inventory inventory, PackagingProviderBlockEntity blockEntity) {
		super(PackagedAutoMenus.PACKAGING_PROVIDER.get(), windowId, inventory, blockEntity);
		addSlot(new SingleStackSlot(itemHandler, itemHandler, 0, 26, 35));
		setupPlayerInventory();
	}
}
