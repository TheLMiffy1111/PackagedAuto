package thelm.packagedauto.inventory;

import cofh.api.energy.IEnergyContainerItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import thelm.packagedauto.tile.TilePackagerExtension;

public class InventoryPackagerExtension extends InventoryBase {

	public static final int[] SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
	public final TilePackagerExtension tile;

	public InventoryPackagerExtension(TilePackagerExtension tile) {
		super(tile, 11);
		this.tile = tile;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if(index < 9 && !tile.getWorldObj().isRemote) {
			if(tile.isWorking && getStackInSlot(index) != null) {
				if(stack == null || !stack.isItemEqual(getStackInSlot(index)) || !tile.isInputValid()) {
					tile.endProcess();
				}
			}
		}
		super.setInventorySlotContents(index, stack);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack stack = super.decrStackSize(index, count);
		if(index < 9 && !tile.getWorldObj().isRemote) {
			if(tile.isWorking && getStackInSlot(index) != null && !tile.isInputValid()) {
				tile.endProcess();
			}
		}
		return stack;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		switch(index) {
		case 9: return false;
		case 10: return stack != null && stack.getItem() instanceof IEnergyContainerItem;
		default: return tile.isWorking ? getStackInSlot(index) != null : true;
		}
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
	public int[] getAccessibleSlotsFromSide(int side) {
		return SLOTS;
	}

	@Override
	public boolean canInsertItem(int index, ItemStack stack, int side) {
		return index < 9;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, int side) {
		return index == 9 || side == 1 && index != 10;
	}
}
