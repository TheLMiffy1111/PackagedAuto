package thelm.packagedauto.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryHelper;
import thelm.packagedauto.slot.SlotBase;
import thelm.packagedauto.slot.SlotPreview;
import thelm.packagedauto.slot.SlotRemoveOnly;
import thelm.packagedauto.tile.TilePackagerExtension;

public class ContainerPackagerExtension extends ContainerTileBase<TilePackagerExtension> {

	public ContainerPackagerExtension(InventoryPlayer player, TilePackagerExtension tile) {
		super(player, tile);
		addSlotToContainer(new SlotBase(inventory, 10, 8, 53));
		for(int i = 0; i < 3; ++i)  {
			for(int j = 0; j < 3; ++j) {
				addSlotToContainer(new SlotBase(inventory, j+i*3, 44+j*18, 17+i*18));
			}
		}
		addSlotToContainer(new SlotRemoveOnly(inventory, 9, 134, 53));
		addSlotToContainer(new SlotPreview(tile.listStackInventory, 0, 134, 17));
		setupPlayerInventory();
	}
}
