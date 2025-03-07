package thelm.packagedauto.container;

import net.minecraft.entity.player.InventoryPlayer;
import thelm.packagedauto.slot.SlotSingleStack;
import thelm.packagedauto.tile.TileCraftingProxy;

public class ContainerCraftingProxy extends ContainerTileBase<TileCraftingProxy> {

	public ContainerCraftingProxy(InventoryPlayer player, TileCraftingProxy tile) {
		super(player, tile);
		addSlotToContainer(new SlotSingleStack(inventory, 0, 80, 17));
		setupPlayerInventory();
	}

	@Override
	public int getPlayerInvX() {
		return 8;
	}

	@Override
	public int getPlayerInvY() {
		return 48;
	}
}
