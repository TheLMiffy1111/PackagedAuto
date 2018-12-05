package thelm.packagedauto.container;

import net.minecraft.entity.player.InventoryPlayer;
import thelm.packagedauto.slot.SlotBase;
import thelm.packagedauto.slot.SlotRemoveOnly;
import thelm.packagedauto.slot.SlotSingleStack;
import thelm.packagedauto.tile.TilePackager;

public class ContainerPackager extends ContainerTileBase<TilePackager> {

	public ContainerPackager(InventoryPlayer player, TilePackager tile) {
		super(player, tile);
		addSlotToContainer(new SlotSingleStack(inventory, 10, 134, 17));
		addSlotToContainer(new SlotBase(inventory, 11, 8, 53));
		for(int i = 0; i < 3; ++i)  {
			for(int j = 0; j < 3; ++j) {
				addSlotToContainer(new SlotBase(inventory, j+i*3, 44+j*18, 17+i*18));
			}
		}
		addSlotToContainer(new SlotRemoveOnly(inventory, 9, 134, 53));
		setupPlayerInventory();
	}
}
