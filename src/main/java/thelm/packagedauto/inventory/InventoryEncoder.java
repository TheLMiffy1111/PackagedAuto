package thelm.packagedauto.inventory;

import net.minecraft.item.ItemStack;
import thelm.packagedauto.api.IRecipeListItem;
import thelm.packagedauto.tile.TileEncoder;

public class InventoryEncoder extends InventoryTileBase {

	public final TileEncoder tile;

	public InventoryEncoder(TileEncoder tile) {
		super(tile, 1);
		this.tile = tile;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return stack.getItem() instanceof IRecipeListItem;
	}
}
