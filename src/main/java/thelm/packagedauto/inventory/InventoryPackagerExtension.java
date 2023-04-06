package thelm.packagedauto.inventory;

import java.util.stream.IntStream;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.CapabilityEnergy;
import thelm.packagedauto.tile.TilePackagerExtension;

public class InventoryPackagerExtension extends InventoryTileBase {

	public final TilePackagerExtension tile;

	public InventoryPackagerExtension(TilePackagerExtension tile) {
		super(tile, 11);
		this.tile = tile;
		slots = IntStream.rangeClosed(0, 9).toArray();
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if(index < 9 && !tile.getWorld().isRemote) {
			if(tile.isWorking && !getStackInSlot(index).isEmpty()) {
				if(stack.isEmpty() || !stack.isItemEqual(getStackInSlot(index)) || !tile.isInputValid()) {
					tile.endProcess();
				}
			}
		}
		super.setInventorySlotContents(index, stack);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack stack = super.decrStackSize(index, count);
		if(index < 9 && !tile.getWorld().isRemote) {
			if(tile.isWorking && !getStackInSlot(index).isEmpty() && !tile.isInputValid()) {
				tile.endProcess();
			}
		}
		return stack;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		switch(index) {
		case 9: return false;
		case 10: return stack.hasCapability(CapabilityEnergy.ENERGY, null);
		default: return tile.isWorking ? !getStackInSlot(index).isEmpty() : true;
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
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return index < 9;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return index == 9 || direction == EnumFacing.UP && index != 10;
	}
}
