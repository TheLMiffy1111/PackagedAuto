package thelm.packagedauto.menu;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntRBTreeMap;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.block.entity.BaseBlockEntity;
import thelm.packagedauto.inventory.BaseItemHandler;
import thelm.packagedauto.slot.FalseCopySlot;
import thelm.packagedauto.util.MiscHelper;

//Large portions of code are taken from CoFHCore
public class BaseMenu<T extends BaseBlockEntity> extends AbstractContainerMenu {

	public final T blockEntity;
	public final Inventory inventory;
	public final BaseItemHandler itemHandler;

	public BaseMenu(MenuType<?> menuType, int windowId, Inventory inventory, T blockEntity) {
		super(menuType, windowId);
		this.blockEntity = blockEntity;
		this.inventory = inventory;
		itemHandler = blockEntity != null ? blockEntity.getItemHandler() : new BaseItemHandler(null, 0);
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
				addSlot(new Slot(inventory, j+i*9+9, xOffset+j*18, yOffset+i*18));
			}
		}
		for(int i = 0; i < 9; i++) {
			addSlot(new Slot(inventory, i, xOffset+i*18, yOffset+58));
		}
	}

	public int getSizeInventory() {
		return itemHandler.getSlots();
	}

	public boolean supportsShiftClick(Player player, int slotIndex) {
		return true;
	}

	public boolean performMerge(Player player, int slotIndex, ItemStack stack) {
		int invBase = getSizeInventory();
		int invFull = slots.size();
		if(slotIndex < invBase) {
			return moveItemStackTo(stack, invBase, invFull, true);
		}
		return moveItemStackTo(stack, 0, invBase, false);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
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
					if(slot.mayPlace(MiscHelper.INSTANCE.cloneStack(stack, rmv)) && ItemStack.isSameItemSameTags(stack, existingStack)) {
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
	public void clicked(int slotId, int mouseButton, ClickType clickType, Player player) {
		Slot slot = slotId < 0 ? null : slots.get(slotId);
		Out:if(slot instanceof FalseCopySlot) {
			ItemStack stack = slot.getItem().copy();
			switch(mouseButton) {
			case 0 -> {
				slot.set(getCarried().isEmpty() ? ItemStack.EMPTY : getCarried().copy());
			}
			case 1 -> {
				if(!getCarried().isEmpty()) {
					ItemStack toPut = getCarried().copy();
					if(ItemStack.isSameItemSameTags(stack, toPut) && stack.getCount() < stack.getMaxStackSize()) {
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
			}
			case 2 -> {
				if(player.isCreative()) {
					break Out;
				}
				if(!stack.isEmpty() && stack.getCount() < stack.getMaxStackSize()) {
					stack.grow(1);
					slot.set(stack);
				}
			}
			}
			return;
		}
		super.clicked(slotId, mouseButton, clickType, player);
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}
}
