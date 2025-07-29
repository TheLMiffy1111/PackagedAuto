package thelm.packagedauto.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import thelm.packagedauto.inventory.InventoryEncoderPattern;
import thelm.packagedauto.slot.SlotBase;
import thelm.packagedauto.slot.SlotFalseCopy;
import thelm.packagedauto.slot.SlotPreview;
import thelm.packagedauto.tile.TileEncoder;

public class ContainerEncoder extends ContainerTileBase<TileEncoder> {

	public InventoryEncoderPattern patternInventory;

	public ContainerEncoder(InventoryPlayer playerInventory, TileEncoder tile) {
		super(playerInventory, tile);
		setupSlots(true);
	}

	public void setupSlots() {
		setupSlots(false);
	}

	protected void setupSlots(boolean init) {
		patternInventory = tile.patternInventories[tile.patternIndex];
		if(init) {
			addSlotToContainer(new SlotBase(inventory, 0, 8, 26));
		}
		for(int i = 0; i < 9; ++i) {
			for(int j = 0; j < 9; ++j) {
				addPatternSlot(patternInventory, i*9+j, 8+j*18, 57+i*18, init);
			}
		}
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				addPatternSlot(patternInventory, 81+i*3+j, 198+j*18, 111+i*18, init);
			}
		}
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				addPatternSlot(patternInventory, 90+i*3+j, 198+j*18, 165+i*18, init);
			}
		}
		if(init) {
			setupPlayerInventory();
		}
	}

	protected void addPatternSlot(InventoryEncoderPattern patternInventory, int index, int x, int y, boolean init) {
		Slot slot;
		int slotIndex = index + 1;
		if((index < 81 || index < 90 && patternInventory.recipeType.canSetOutput()) && patternInventory.recipeType.getEnabledSlots().contains(index)) {
			slot = new SlotFalseCopy(patternInventory, index, x, y);
		}
		else {
			slot = new SlotPreview(patternInventory, index, x, y);
		}
		slot.slotNumber = slotIndex;
		if(init) {
			addSlotToContainer(slot);
		}
		else {
			inventorySlots.set(slotIndex, slot);
		}
	}

	@Override
	public int getPlayerInvX() {
		return 49;
	}

	@Override
	public int getPlayerInvY() {
		return 232;
	}

	@Override
	public int getSizeInventory() {
		return 91;
	}
}
