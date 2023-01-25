package thelm.packagedauto.container;

import net.minecraft.entity.player.InventoryPlayer;
import thelm.packagedauto.slot.SlotBase;
import thelm.packagedauto.slot.SlotSingleStack;
import thelm.packagedauto.tile.TileUnpackager;

public class ContainerUnpackager extends ContainerBase<TileUnpackager> {

	public ContainerUnpackager(InventoryPlayer playerInventory, TileUnpackager tile) {
		super(playerInventory, tile);
		addSlotToContainer(new SlotSingleStack(inventory, 9, 26, 17));
		addSlotToContainer(new SlotBase(inventory, 10, 8, 53));
		for(int i = 0; i < 3; ++i)  {
			for(int j = 0; j < 3; ++j) {
				addSlotToContainer(new SlotBase(inventory, j+i*3, 44+j*18, 17+i*18));
			}
		}
		setupPlayerInventory();
	}
}
