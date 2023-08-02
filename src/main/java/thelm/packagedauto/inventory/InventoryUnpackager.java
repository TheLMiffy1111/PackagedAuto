package thelm.packagedauto.inventory;

import java.util.Arrays;
import java.util.stream.IntStream;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.CapabilityEnergy;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IRecipeListItem;
import thelm.packagedauto.tile.TileUnpackager;
import thelm.packagedauto.tile.TileUnpackager.PackageTracker;

public class InventoryUnpackager extends InventoryTileBase {

	public final TileUnpackager tile;

	public InventoryUnpackager(TileUnpackager tile) {
		super(tile, 11);
		this.tile = tile;
		slots = IntStream.rangeClosed(0, 8).toArray();
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		super.setInventorySlotContents(index, stack);
		if(index == 9) {
			updateRecipeList();
		}
		else if(index != 10) {
			clearRejectedIndexes();
		}
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack stack = super.decrStackSize(index, count);
		if(index == 9) {
			updateRecipeList();
		}
		else if(index != 10) {
			clearRejectedIndexes();
		}
		return stack;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		switch(index) {
		case 9: return stack.getItem() instanceof IRecipeListItem;
		case 10: return stack.hasCapability(CapabilityEnergy.ENERGY, null);
		default: return stack.getItem() instanceof IPackageItem;
		}
	}

	@Override
	public int getField(int id) {
		if(id < 10) {
			return tile.trackers[id].getSyncValue();
		}
		switch(id) {
		case 10: return tile.blocking ? 1 : 0;
		case 11: return tile.getEnergyStorage().getEnergyStored();
		default: return 0;
		}
	}

	@Override
	public void setField(int id, int value) {
		if(id < 10) {
			tile.trackers[id].setSyncValue(value);
		}
		switch(id) {
		case 10:
			tile.blocking = value != 0;
			break;
		case 11:
			tile.getEnergyStorage().setEnergyStored(value);
			break;
		}
	}

	@Override
	public int getFieldCount() {
		return 12;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		updateRecipeList();
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return index != 9 && index != 10;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return index != 9 && index != 10 && direction == EnumFacing.UP && !Arrays.stream(tile.trackers).anyMatch(t->t.isEmpty());
	}

	public void updateRecipeList() {
		tile.recipeList.clear();
		ItemStack listStack = getStackInSlot(9);
		if(listStack.getItem() instanceof IRecipeListItem) {
			tile.recipeList.addAll(((IRecipeListItem)listStack.getItem()).getRecipeList(listStack).getRecipeList());
		}
		if(tile.getWorld() != null && !tile.getWorld().isRemote && tile.hostHelper != null) {
			tile.hostHelper.postPatternChange();
		}
	}

	public void clearRejectedIndexes() {
		for(PackageTracker tracker : tile.trackers) {
			tracker.clearRejectedIndexes();
		}
	}
}
