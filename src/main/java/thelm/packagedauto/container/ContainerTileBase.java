package thelm.packagedauto.container;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntRBTreeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.inventory.InventoryTileBase;
import thelm.packagedauto.slot.SlotFalseCopy;
import thelm.packagedauto.tile.TileBase;

//Large portions of code are taken from CoFHCore
public class ContainerTileBase<TILE extends TileBase> extends Container {

	public final TILE tile;
	public final InventoryPlayer playerInventory;
	public final InventoryTileBase inventory;
	public final Int2IntMap prevSyncValues = new Int2IntRBTreeMap();

	public ContainerTileBase(InventoryPlayer playerInventory, TILE tile) {
		this.tile = tile;
		this.playerInventory = playerInventory;
		this.inventory = tile != null ? tile.getInventory() : new InventoryTileBase(null, 0);
	}

	public int getPlayerInvY() {
		return 84;
	}

	public int getPlayerInvX() {
		return 8;
	}

	public void setupPlayerInventory() {
		int xOffset = getPlayerInvX();
		int yOffset = getPlayerInvY();
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(playerInventory, j+i*9+9, xOffset+j*18, yOffset+i*18));
			}
		}
		for(int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(playerInventory, i, xOffset+i*18, yOffset+58));
		}
	}

	public int getSizeInventory() {
		return inventory.getSizeInventory();
	}

	public boolean supportsShiftClick(EntityPlayer player, int slotIndex) {
		return true;
	}

	public boolean performMerge(EntityPlayer player, int slotIndex, ItemStack stack) {
		int invBase = getSizeInventory();
		int invFull = inventorySlots.size();
		if(slotIndex < invBase) {
			return mergeItemStack(stack, invBase, invFull, true);
		}
		return mergeItemStack(stack, 0, invBase, false);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
		if(!supportsShiftClick(player, slotIndex)) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(slotIndex);
		if(slot != null && slot.getHasStack()) {
			ItemStack stackInSlot = slot.getStack();
			stack = stackInSlot.copy();
			if(!performMerge(player, slotIndex, stackInSlot)) {
				return ItemStack.EMPTY;
			}
			slot.onSlotChange(stackInSlot, stack);
			if(stackInSlot.getCount() <= 0) {
				slot.putStack(ItemStack.EMPTY);
			}
			else {
				slot.putStack(stackInSlot);
			}
			if(stackInSlot.getCount() == stack.getCount()) {
				return ItemStack.EMPTY;
			}
			slot.onTake(player, stackInSlot);
		}
		return stack;
	}

	@Override
	protected boolean mergeItemStack(ItemStack stack, int slotMin, int slotMax, boolean ascending) {
		boolean successful = false;
		int i = !ascending ? slotMin : slotMax - 1;
		int iterOrder = !ascending ? 1 : -1;
		Slot slot;
		ItemStack existingStack;
		if(stack.isStackable()) {
			while(stack.getCount() > 0 && (!ascending && i < slotMax || ascending && i >= slotMin)) {
				slot = inventorySlots.get(i);
				if(slot instanceof SlotFalseCopy) {
					i += iterOrder;
					continue;
				}
				existingStack = slot.getStack();
				if(!existingStack.isEmpty()) {
					int maxStack = Math.min(stack.getMaxStackSize(), slot.getSlotStackLimit());
					int rmv = Math.min(maxStack, stack.getCount());
					if(slot.isItemValid(MiscUtil.cloneStack(stack, rmv)) && existingStack.getItem().equals(stack.getItem()) && (!stack.getHasSubtypes() || stack.getItemDamage() == existingStack.getItemDamage()) && ItemStack.areItemStackTagsEqual(stack, existingStack)) {
						int existingSize = existingStack.getCount() + stack.getCount();
						if(existingSize <= maxStack) {
							stack.setCount(0);
							existingStack.setCount(existingSize);
							slot.putStack(existingStack);
							successful = true;
						}
						else if(existingStack.getCount() < maxStack) {
							stack.shrink(maxStack - existingStack.getCount());
							existingStack.setCount(maxStack);
							slot.putStack(existingStack);
							successful = true;
						}
					}
				}
				i += iterOrder;
			}
		}
		if(stack.getCount() > 0) {
			i = !ascending ? slotMin : slotMax - 1;
			while(stack.getCount() > 0 && (!ascending && i < slotMax || ascending && i >= slotMin)) {
				slot = inventorySlots.get(i);
				if(slot instanceof SlotFalseCopy) {
					i += iterOrder;
					continue;
				}
				existingStack = slot.getStack();
				if(existingStack.isEmpty()) {
					int maxStack = Math.min(stack.getMaxStackSize(), slot.getSlotStackLimit());
					int rmv = Math.min(maxStack, stack.getCount());
					if(slot.isItemValid(MiscUtil.cloneStack(stack, rmv))) {
						existingStack = stack.splitStack(rmv);
						slot.putStack(existingStack);
						successful = true;
					}
				}
				i += iterOrder;
			}
		}
		return successful;
	}

	@Override
	public ItemStack slotClick(int slotId, int mouseButton, ClickType clickType, EntityPlayer player) {
		Slot slot = slotId < 0 ? null : inventorySlots.get(slotId);
		Out:if(slot instanceof SlotFalseCopy) {
			ItemStack stack = slot.getStack().copy();
			switch(mouseButton) {
			case 0:
				slot.putStack(player.inventory.getItemStack().isEmpty() ? ItemStack.EMPTY : player.inventory.getItemStack().copy());
				break;
			case 1:
				if(!player.inventory.getItemStack().isEmpty()) {
					ItemStack toPut = player.inventory.getItemStack().copy();
					if(stack.getItem() == toPut.getItem() && stack.getItemDamage() == toPut.getItemDamage() &&
							ItemStack.areItemStackShareTagsEqual(stack, toPut) && stack.getCount() < stack.getMaxStackSize()) {
						stack.grow(1);
						slot.putStack(stack);
					}
					else {
						toPut.setCount(1);
						slot.putStack(toPut);
					}
				}
				else if(!stack.isEmpty()) {
					stack.shrink(1);
					slot.putStack(stack);
				}
				break;
			case 2:
				if(player.capabilities.isCreativeMode) {
					break Out;
				}
				if(!stack.isEmpty() && stack.getCount() < stack.getMaxStackSize()) {
					stack.grow(1);
					slot.putStack(stack);
				}
				break;
			}
			return player.inventory.getItemStack();
		}
		return super.slotClick(slotId, mouseButton, clickType, player);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return inventory.isUsableByPlayer(player);
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		for(IContainerListener listener : listeners) {
			for(int i = 0; i < inventory.getFieldCount(); ++i) {
				if(!prevSyncValues.containsKey(i) || prevSyncValues.get(i) != inventory.getField(i)) {
					listener.sendWindowProperty(this, i, inventory.getField(i));
				}
			}
		}
		for(int i = 0; i < inventory.getFieldCount(); ++i) {
			prevSyncValues.put(i, inventory.getField(i));
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void updateProgressBar(int id, int data) {
		inventory.setField(id, data);
	}
}
