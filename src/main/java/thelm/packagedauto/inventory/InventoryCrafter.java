package thelm.packagedauto.inventory;

import cofh.api.energy.IEnergyContainerItem;
import net.minecraft.item.ItemStack;
import thelm.packagedauto.tile.TileCrafter;

public class InventoryCrafter extends InventoryBase {

	public final TileCrafter tile;

	public InventoryCrafter(TileCrafter tile) {
		super(tile, 11);
		this.tile = tile;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if(index == 10) {
			return stack != null && stack.getItem() instanceof IEnergyContainerItem;
		}
		return false;
	}

	@Override
	public int getField(int id) {
		switch(id) {
		case 0: return tile.remainingProgress;
		case 1: return tile.isWorking ? 1 : 0;
		default: return 0;
		}
	}

	@Override
	public void setField(int id, int value) {
		switch(id) {
		case 0:
			tile.remainingProgress = value;
			break;
		case 1:
			tile.isWorking = value != 0;
			break;
		}
	}

	@Override
	public int getFieldCount() {
		return 2;
	}

	@Override
	public boolean canInsertItem(int index, ItemStack stack, int side) {
		return false;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, int side) {
		return tile.isWorking ? index == 9 : index != 10;
	}
}
