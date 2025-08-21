package thelm.packagedauto.container;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
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

	public int getPlayerInvX() {
		return 8;
	}

	public int getPlayerInvY() {
		return 84;
	}

	protected void setupPlayerInventory() {
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

	protected int dragCount = 0;
	protected List<Slot> dragSlots = new ArrayList<>();

	@Override
	public ItemStack clicked(int slotId, int clickSubtype, ClickType clickType, PlayerEntity player) {
		if(clickType == ClickType.QUICK_CRAFT) {
			if(clickSubtype == 1 && slotId >= 0) {
				dragCount++;
				Slot slot = slots.get(slotId);
				if(slot instanceof FalseCopySlot) {
					dragSlots.add(slot);
					return player.inventory.getCarried();
				}
			}
			else if(clickSubtype == 2 && !dragSlots.isEmpty()) {
				ItemStack toPut = player.inventory.getCarried().copy();
				toPut.setCount(toPut.getCount()/dragCount);
				for(Slot slot : dragSlots) {
					slot.set(toPut.copy());
				}
				dragCount = 0;
				dragSlots.clear();
			}
		}
		else if(clickType != ClickType.CLONE && slotId >= 0) {
			Slot slot = slots.get(slotId);
			if(slot instanceof FalseCopySlot) {
				if(clickType == ClickType.QUICK_MOVE) {
					slot.set(ItemStack.EMPTY);
				}
				else {
					ItemStack toPut = player.inventory.getCarried().copy();
					ItemStack stack = slot.getItem().copy();
					if(clickSubtype == 0) {
						slot.set(toPut);
					}
					else if(clickSubtype == 1) {
						if(stack.isEmpty()) {
							if(!toPut.isEmpty()) {
								toPut.setCount(1);
							}
							slot.set(toPut);
						}
						else if(stack.sameItem(toPut) && ItemStack.tagMatches(stack, toPut) && stack.getCount() < stack.getMaxStackSize()) {
							stack.grow(1);
							slot.set(stack);
						}
						else {
							stack.shrink(1);
							slot.set(stack);
						}
					}
				}
				return player.inventory.getCarried();
			}
		}
		return super.clicked(slotId, clickSubtype, clickType, player);
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		if(tile != null) {
			BlockPos pos = tile.getBlockPos();
			return tile.getLevel().getBlockEntity(pos) == tile && tile.getBlockPos().distSqr(player.position(), true) <= 64;
		}
		return true;
	}
}
