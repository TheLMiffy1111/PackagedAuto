package thelm.packagedauto.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.items.SlotItemHandler;
import thelm.packagedauto.block.entity.FluidPackageFillerBlockEntity;
import thelm.packagedauto.menu.factory.PositionalBlockEntityMenuFactory;
import thelm.packagedauto.slot.RemoveOnlySlot;

public class FluidPackageFillerMenu extends BaseMenu<FluidPackageFillerBlockEntity> {

	public static final MenuType<FluidPackageFillerMenu> TYPE_INSTANCE = IForgeMenuType.create(new PositionalBlockEntityMenuFactory<>(FluidPackageFillerMenu::new));

	public FluidPackageFillerMenu(int windowId, Inventory inventory, FluidPackageFillerBlockEntity blockEntity) {
		super(TYPE_INSTANCE, windowId, inventory, blockEntity);
		addSlot(new SlotItemHandler(itemHandler, 2, 8, 53));
		addSlot(new SlotItemHandler(itemHandler, 0, 44, 35));
		addSlot(new RemoveOnlySlot(itemHandler, 1, 134, 35));
		setupPlayerInventory();
	}
}
