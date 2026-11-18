package thelm.packagedauto.menu;

import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import thelm.packagedauto.block.entity.FluidPackageFillerBlockEntity;
import thelm.packagedauto.slot.RemoveOnlySlot;

public class FluidPackageFillerMenu extends BaseMenu<FluidPackageFillerBlockEntity> {

	public FluidPackageFillerMenu(int windowId, Inventory inventory, FluidPackageFillerBlockEntity blockEntity) {
		super(PackagedAutoMenus.FLUID_PACKAGE_FILLER.get(), windowId, inventory, blockEntity);
		addSlot(new ResourceHandlerSlot(itemHandler, itemHandler, 2, 8, 53));
		addSlot(new ResourceHandlerSlot(itemHandler, itemHandler, 0, 44, 35));
		addSlot(new RemoveOnlySlot(itemHandler, itemHandler, 1, 134, 35));
		setupPlayerInventory();
	}
}
