package thelm.packagedauto.inventory;

import java.util.stream.IntStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import thelm.packagedauto.tile.TileBase;

public class InventoryTileBase implements ISidedInventory {

	public final NonNullList<ItemStack> stacks;
	public final TileBase tile;
	public int[] slots;

	public InventoryTileBase(TileBase tile, int size) {
		this.tile = tile;
		stacks = NonNullList.withSize(size, ItemStack.EMPTY);
		slots = IntStream.range(0, size).toArray();
	}

	@Override
	public int getSizeInventory() {
		return stacks.size();
	}

	@Override
	public boolean isEmpty() {
		for(ItemStack stack : stacks) {
			if(!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return index >= 0 && index < stacks.size() ? stacks.get(index) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack stack = ItemStackHelper.getAndSplit(stacks, index, count);
		markDirty();
		return stack;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		ItemStack stack = ItemStackHelper.getAndRemove(stacks, index);
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

	public void syncTile(boolean rerender) {
		if(tile != null) {
			tile.syncTile(rerender);
		}
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return tile != null && player.getDistanceSq(tile.getPos().getX()+0.5D, tile.getPos().getY()+0.5D, tile.getPos().getZ()+0.5D) <= 64D;
	}

	@Override
	public void openInventory(EntityPlayer player) {}

	@Override
	public void closeInventory(EntityPlayer player) {}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
		stacks.clear();
	}

	@Override
	public String getName() {
		return tile != null ? tile.getName() : "[null]";
	}

	@Override
	public boolean hasCustomName() {
		return tile != null && tile.hasCustomName();
	}

	@Override
	public ITextComponent getDisplayName() {
		return tile != null ? tile.getDisplayName() : new TextComponentString("[null]");
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return slots;
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return true;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return true;
	}

	public void readFromNBT(NBTTagCompound nbt) {
		stacks.clear();
		ItemStackHelper.loadAllItems(nbt, stacks);
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		return ItemStackHelper.saveAllItems(nbt, stacks);
	}
}
