package thelm.packagedauto.inventory;

import java.util.Arrays;
import java.util.stream.IntStream;

import cofh.api.energy.IEnergyContainerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackageRecipeListItem;
import thelm.packagedauto.tile.TileUnpackager;
import thelm.packagedauto.tile.TileUnpackager.PackageTracker;

public class InventoryUnpackager extends InventoryBase {

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
		case 9: return stack != null && stack.getItem() instanceof IPackageRecipeListItem;
		case 10: return stack != null && stack.getItem() instanceof IEnergyContainerItem;
		default: return stack != null && stack.getItem() instanceof IPackageItem;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		updateRecipeList();
	}

	@Override
	public boolean canInsertItem(int index, ItemStack stack, int side) {
		return index != 9 && index != 10;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, int side) {
		return index != 9 && index != 10 && side == 1 && !Arrays.stream(tile.trackers).anyMatch(t->t.isEmpty());
	}

	public void updateRecipeList() {
		tile.recipeList.clear();
		ItemStack listStack = getStackInSlot(9);
		if(listStack != null && listStack.getItem() instanceof IPackageRecipeListItem) {
			tile.recipeList.addAll(((IPackageRecipeListItem)listStack.getItem()).getRecipeList(listStack).getRecipeList());
		}
		if(tile.getWorldObj() != null && !tile.getWorldObj().isRemote && tile.hostHelper != null) {
			tile.hostHelper.postPatternChange();
		}
	}

	public void clearRejectedIndexes() {
		for(PackageTracker tracker : tile.trackers) {
			tracker.clearRejectedIndexes();
		}
	}
}
