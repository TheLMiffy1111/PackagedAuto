package thelm.packagedauto.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import thelm.packagedauto.slot.SlotPreview;
import thelm.packagedauto.tile.TileBase;
import thelm.packagedauto.util.MiscHelper;

public class ContainerAmountSpecifying extends ContainerBase<TileBase> {

	public ContainerAmountSpecifying(InventoryPlayer playerInventory, ItemStack stack) {
		super(playerInventory, null);
		InventoryBasic itemInventory = new InventoryBasic("[Null]", true, 1);
		itemInventory.setInventorySlotContents(0, MiscHelper.INSTANCE.cloneStack(stack, 1));
		addSlotToContainer(new SlotPreview(itemInventory, 0, 89, 48));
	}

	@Override
	public int getPlayerInvX() {
		return 0;
	}

	@Override
	public int getPlayerInvY() {
		return 0;
	}

	@Override
	public int getSizeInventory() {
		return 0;
	}
}
