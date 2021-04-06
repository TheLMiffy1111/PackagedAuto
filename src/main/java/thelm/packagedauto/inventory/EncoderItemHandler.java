package thelm.packagedauto.inventory;

import net.minecraft.item.ItemStack;
import thelm.packagedauto.api.IPackageRecipeListItem;
import thelm.packagedauto.tile.EncoderTile;

public class EncoderItemHandler extends BaseItemHandler<EncoderTile> {

	public EncoderItemHandler(EncoderTile tile) {
		super(tile, 1);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return stack.getItem() instanceof IPackageRecipeListItem;
	}
}
