package thelm.packagedauto.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import thelm.packagedauto.tile.CrafterTile;

public class CrafterRemoveOnlySlot extends SlotItemHandler {

	public final CrafterTile tile;

	public CrafterRemoveOnlySlot(CrafterTile tile, int index, int x, int y) {
		super(tile.getItemHandler(), index, x, y);
		this.tile = tile;
	}

	@Override
	public boolean canTakeStack(PlayerEntity playerIn) {
		return !tile.isWorking;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return false;
	}
}
