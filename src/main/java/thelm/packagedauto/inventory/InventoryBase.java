package thelm.packagedauto.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import thelm.packagedauto.tile.TileBase;
import thelm.packagedauto.util.MiscHelper;

public class InventoryBase implements ISidedInventory {

	public final List<ItemStack> stacks;
	public final TileBase tile;
	public int[] slots;

	public InventoryBase(TileBase tile, int size) {
		this.tile = tile;
		stacks = new ArrayList<>(size);
		while(stacks.size() < size) {
			stacks.add(null);
		}
		slots = IntStream.range(0, size).toArray();
	}

	@Override
	public int getSizeInventory() {
		return stacks.size();
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return index >= 0 && index < stacks.size() ? stacks.get(index) : null;
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack stack = index >= 0 && index < stacks.size() && stacks.get(index) != null && count > 0 ? stacks.get(index).splitStack(count) : null;
		markDirty();
		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int index) {
		ItemStack stack = index >= 0 && index < stacks.size() ? stacks.set(index, null) : null;
		markDirty();
		return stack;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if(index >= 0 && index < stacks.size()) {
			stacks.set(index, stack);
		}
		markDirty();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void markDirty() {
		if(tile != null) {
			tile.markDirty();
		}
	}

	public void syncTile() {
		if(tile != null) {
			tile.syncTile();
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return tile != null && player.getDistanceSq(tile.xCoord+0.5D, tile.yCoord+0.5D, tile.zCoord+0.5D) <= 64D;
	}

	@Override
	public void openInventory() {}

	@Override
	public void closeInventory() {}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
	}

	public int getField(int index) {
		return 0;
	}

	public void setField(int index, int value) {}

	public int getFieldCount() {
		return 0;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return slots;
	}

	@Override
	public boolean canInsertItem(int index, ItemStack stack, int side) {
		return true;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, int side) {
		return true;
	}

	@Override
	public String getInventoryName() {
		return tile.getInventoryName();
	}

	@Override
	public boolean hasCustomInventoryName() {
		return tile.hasCustomInventoryName();
	}

	public void readFromNBT(NBTTagCompound nbt) {
		MiscHelper.INSTANCE.loadAllItems(nbt.getTagList("Items", 10), stacks);
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setTag("Items", MiscHelper.INSTANCE.saveAllItems(new NBTTagList(), stacks));
		return nbt;
	}
}
