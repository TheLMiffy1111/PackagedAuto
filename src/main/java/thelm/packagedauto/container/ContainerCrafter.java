package thelm.packagedauto.container;

import net.minecraft.entity.player.InventoryPlayer;
import thelm.packagedauto.slot.SlotBase;
import thelm.packagedauto.slot.SlotCrafterRemoveOnly;
import thelm.packagedauto.slot.SlotRemoveOnly;
import thelm.packagedauto.tile.TileCrafter;

public class ContainerCrafter extends ContainerBase<TileCrafter> {

	public ContainerCrafter(InventoryPlayer playerInventory, TileCrafter tile) {
		super(playerInventory, tile);
		addSlotToContainer(new SlotBase(inventory, 10, 8, 53));
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				addSlotToContainer(new SlotCrafterRemoveOnly(tile, i*3+j, 44+j*18, 17+i*18));
			}
		}
		addSlotToContainer(new SlotRemoveOnly(inventory, 9, 134, 35));
		setupPlayerInventory();
	}
}
