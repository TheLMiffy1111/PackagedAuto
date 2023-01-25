package thelm.packagedauto.inventory;

import net.minecraft.item.ItemStack;
import thelm.packagedauto.api.IPackageRecipeListItem;
import thelm.packagedauto.tile.TileEncoder;

public class InventoryEncoder extends InventoryBase {

	public final TileEncoder tile;

	public InventoryEncoder(TileEncoder tile) {
		super(tile, 1);
		this.tile = tile;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return stack != null && stack.getItem() instanceof IPackageRecipeListItem;
	}
}
