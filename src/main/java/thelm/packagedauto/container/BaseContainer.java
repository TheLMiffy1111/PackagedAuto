package thelm.packagedauto.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIntArray;
import thelm.packagedauto.inventory.BaseItemHandler;
import thelm.packagedauto.slot.FalseCopySlot;
import thelm.packagedauto.tile.BaseTile;
import thelm.packagedauto.util.MiscHelper;
import thelm.packagedauto.util.OptionalIntReferenceHolder;

//Large portions of code are taken from CoFHCore
public class BaseContainer<T extends BaseTile> extends Container {

	public final T tile;
	public final PlayerInventory playerInventory;
	public final BaseItemHandler<?> itemHandler;

	public BaseContainer(ContainerType<?> containerType, int windowId, PlayerInventory playerInventory, T tile) {
		super(containerType, windowId);
		this.tile = tile;
		this.playerInventory = playerInventory;
		itemHandler = tile != null ? tile.getItemHandler() : new BaseItemHandler<>(null, 0);
		addDataSlots(itemHandler);
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
				addSlot(new Slot(playerInventory, j+i*9+9, xOffset+j*18, yOffset+i*18));
			}
		}
		for(int i = 0; i < 9; i++) {
			addSlot(new Slot(playerInventory, i, xOffset+i*18, yOffset+58));
		}
	}

	@Override
	protected void addDataSlots(IIntArray array) {
		for(int i = 0; i < array.getCount(); ++i) {
			addDataSlot(OptionalIntReferenceHolder.of(array, i));
		}
	}

	public int getContainerSize() {
		return itemHandler.getSlots();
	}

	public boolean supportsShiftClick(PlayerEntity player, int slotIndex) {
		return true;
	}

	public boolean performMerge(PlayerEntity player, int slotIndex, ItemStack stack) {
		int invBase = getContainerSize();
		int invFull = slots.size();
		if(slotIndex < invBase) {
			return moveItemStackTo(stack, invBase, invFull, true);
		}
		return moveItemStackTo(stack, 0, invBase, false);
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity player, int slotIndex) {
		if(!supportsShiftClick(player, slotIndex)) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = slots.get(slotIndex);
		if(slot != null && slot.hasItem()) {
			ItemStack stackInSlot = slot.getItem();
			stack = stackInSlot.copy();
			if(!performMerge(player, slotIndex, stackInSlot)) {
				return ItemStack.EMPTY;
			}
			slot.onQuickCraft(stackInSlot, stack);
			if(stackInSlot.getCount() <= 0) {
				slot.set(ItemStack.EMPTY);
			}
			else {
				slot.set(stackInSlot);
			}
			if(stackInSlot.getCount() == stack.getCount()) {
				return ItemStack.EMPTY;
			}
			slot.onTake(player, stackInSlot);
		}
		return stack;
	}

	@Override
	protected boolean moveItemStackTo(ItemStack stack, int slotMin, int slotMax, boolean ascending) {
		boolean successful = false;
		int i = !ascending ? slotMin : slotMax - 1;
		int iterOrder = !ascending ? 1 : -1;
		Slot slot;
		ItemStack existingStack;
		if(stack.isStackable()) {
			while(stack.getCount() > 0 && (!ascending && i < slotMax || ascending && i >= slotMin)) {
				slot = slots.get(i);
				if(slot instanceof FalseCopySlot) {
					i += iterOrder;
					continue;
				}
				existingStack = slot.getItem();
				if(!existingStack.isEmpty()) {
					int maxStack = Math.min(stack.getMaxStackSize(), slot.getMaxStackSize());
					int rmv = Math.min(maxStack, stack.getCount());
					if(slot.mayPlace(MiscHelper.INSTANCE.cloneStack(stack, rmv)) && existingStack.getItem().equals(stack.getItem()) && ItemStack.tagMatches(stack, existingStack)) {
						int existingSize = existingStack.getCount() + stack.getCount();
						if(existingSize <= maxStack) {
							stack.setCount(0);
							existingStack.setCount(existingSize);
							slot.set(existingStack);
							successful = true;
						}
						else if(existingStack.getCount() < maxStack) {
							stack.shrink(maxStack - existingStack.getCount());
							existingStack.setCount(maxStack);
							slot.set(existingStack);
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
				slot = slots.get(i);
				if(slot instanceof FalseCopySlot) {
					i += iterOrder;
					continue;
				}
				existingStack = slot.getItem();
				if(existingStack.isEmpty()) {
					int maxStack = Math.min(stack.getMaxStackSize(), slot.getMaxStackSize());
					int rmv = Math.min(maxStack, stack.getCount());
					if(slot.mayPlace(MiscHelper.INSTANCE.cloneStack(stack, rmv))) {
						existingStack = stack.split(rmv);
						slot.set(existingStack);
						successful = true;
					}
				}
				i += iterOrder;
			}
		}
		return successful;
	}

	@Override
	public ItemStack clicked(int slotId, int mouseButton, ClickType clickType, PlayerEntity player) {
		Slot slot = slotId < 0 ? null : slots.get(slotId);
		Out:if(slot instanceof FalseCopySlot) {
			ItemStack stack = slot.getItem().copy();
			switch(mouseButton) {
			case 0:
				slot.set(player.inventory.getCarried().isEmpty() ? ItemStack.EMPTY : player.inventory.getCarried().copy());
				break;
			case 1:
				if(!player.inventory.getCarried().isEmpty()) {
					ItemStack toPut = player.inventory.getCarried().copy();
					if(stack.getItem() == toPut.getItem() &&
							ItemStack.tagMatches(stack, toPut) && stack.getCount() < stack.getMaxStackSize()) {
						stack.grow(1);
						slot.set(stack);
					}
					else {
						toPut.setCount(1);
						slot.set(toPut);
					}
				}
				else if(!stack.isEmpty()) {
					stack.shrink(1);
					slot.set(stack);
				}
				break;
			case 2:
				if(player.isCreative()) {
					break Out;
				}
				if(!stack.isEmpty() && stack.getCount() < stack.getMaxStackSize()) {
					stack.grow(1);
					slot.set(stack);
				}
				break;
			}
			return player.inventory.getCarried();
		}
		return super.clicked(slotId, mouseButton, clickType, player);
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return true;
	}
}
