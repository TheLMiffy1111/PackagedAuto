package thelm.packagedauto.container;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thelm.packagedauto.inventory.InventoryBase;
import thelm.packagedauto.slot.SlotFalseCopy;
import thelm.packagedauto.tile.TileBase;
import thelm.packagedauto.util.MiscHelper;

//Large portions of code are taken from CoFHCore
public class ContainerBase<TILE extends TileBase> extends Container {

	public final TILE tile;
	public final InventoryPlayer playerInventory;
	public final InventoryBase inventory;
	public final Map<Integer, Integer> prevSyncValues = new HashMap<>();

	public ContainerBase(InventoryPlayer playerInventory, TILE tile) {
		this.tile = tile;
		this.playerInventory = playerInventory;
		this.inventory = tile != null ? tile.getInventory() : new InventoryBase(null, 0);
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
			return null;
		}
		ItemStack stack = null;
		Slot slot = (Slot)inventorySlots.get(slotIndex);
		if(slot != null && slot.getHasStack()) {
			ItemStack stackInSlot = slot.getStack();
			stack = stackInSlot.copy();
			if(!performMerge(player, slotIndex, stackInSlot)) {
				return null;
			}
			slot.onSlotChange(stackInSlot, stack);
			if(stackInSlot.stackSize <= 0) {
				slot.putStack(null);
			}
			else {
				slot.putStack(stackInSlot);
			}
			if(stackInSlot.stackSize == stack.stackSize) {
				return null;
			}
			slot.onPickupFromSlot(player, stackInSlot);
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
			while(stack.stackSize > 0 && (!ascending && i < slotMax || ascending && i >= slotMin)) {
				slot = (Slot)inventorySlots.get(i);
				if(slot instanceof SlotFalseCopy) {
					i += iterOrder;
					continue;
				}
				existingStack = slot.getStack();
				if(existingStack != null) {
					int maxStack = Math.min(stack.getMaxStackSize(), slot.getSlotStackLimit());
					int rmv = Math.min(maxStack, stack.stackSize);
					if(slot.isItemValid(MiscHelper.INSTANCE.cloneStack(stack, rmv)) && existingStack.getItem().equals(stack.getItem()) && (!stack.getHasSubtypes() || stack.getItemDamage() == existingStack.getItemDamage()) && ItemStack.areItemStackTagsEqual(stack, existingStack)) {
						int existingSize = existingStack.stackSize + stack.stackSize;
						if(existingSize <= maxStack) {
							stack.stackSize = 0;
							existingStack.stackSize = existingSize;
							slot.putStack(existingStack);
							successful = true;
						}
						else if(existingStack.stackSize < maxStack) {
							stack.stackSize -= maxStack - existingStack.stackSize;
							existingStack.stackSize = maxStack;
							slot.putStack(existingStack);
							successful = true;
						}
					}
				}
				i += iterOrder;
			}
		}
		if(stack.stackSize > 0) {
			i = !ascending ? slotMin : slotMax - 1;
			while(stack.stackSize > 0 && (!ascending && i < slotMax || ascending && i >= slotMin)) {
				slot = (Slot)inventorySlots.get(i);
				if(slot instanceof SlotFalseCopy) {
					i += iterOrder;
					continue;
				}
				existingStack = slot.getStack();
				if(existingStack == null) {
					int maxStack = Math.min(stack.getMaxStackSize(), slot.getSlotStackLimit());
					int rmv = Math.min(maxStack, stack.stackSize);
					if(slot.isItemValid(MiscHelper.INSTANCE.cloneStack(stack, rmv))) {
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
	public ItemStack slotClick(int slotId, int mouseButton, int clickType, EntityPlayer player) {
		Slot slot = slotId < 0 ? null : (Slot)inventorySlots.get(slotId);
		Out:if(slot instanceof SlotFalseCopy) {
			ItemStack stack = slot.getStack() == null ? null : slot.getStack().copy();
			switch(mouseButton) {
			case 0:
				slot.putStack(player.inventory.getItemStack() == null ? null : player.inventory.getItemStack().copy());
				break;
			case 1:
				if(player.inventory.getItemStack() != null) {
					ItemStack toPut = player.inventory.getItemStack().copy();
					if(stack != null && stack.getItem() == toPut.getItem() && stack.getItemDamage() == toPut.getItemDamage() &&
							ItemStack.areItemStackTagsEqual(stack, toPut) && stack.stackSize < stack.getMaxStackSize()) {
						stack.stackSize += 1;
						slot.putStack(stack);
					}
					else {
						toPut.stackSize = 1;
						slot.putStack(toPut);
					}
				}
				else if(stack != null) {
					stack.stackSize -= 1;
					slot.putStack(stack);
				}
				break;
			case 2:
				if(player.capabilities.isCreativeMode) {
					break Out;
				}
				if(stack != null && stack.stackSize < stack.getMaxStackSize()) {
					stack.stackSize += 1;
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
		return inventory.isUseableByPlayer(player);
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		for(ICrafting listener : (List<ICrafting>)crafters) {
			for(int i = 0; i < inventory.getFieldCount(); ++i) {
				if(!prevSyncValues.containsKey(i) || prevSyncValues.get(i) != inventory.getField(i)) {
					listener.sendProgressBarUpdate(this, i, inventory.getField(i));
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
