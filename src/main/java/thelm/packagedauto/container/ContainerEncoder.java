package thelm.packagedauto.container;

import net.minecraft.entity.player.InventoryPlayer;
import thelm.packagedauto.inventory.InventoryEncoderPattern;
import thelm.packagedauto.slot.SlotBase;
import thelm.packagedauto.slot.SlotFalseCopy;
import thelm.packagedauto.slot.SlotPreview;
import thelm.packagedauto.tile.TileEncoder;

public class ContainerEncoder extends ContainerTileBase<TileEncoder> {

	public InventoryEncoderPattern patternInventory;

	public ContainerEncoder(InventoryPlayer playerInventory, TileEncoder tile) {
		super(playerInventory, tile);
		setupSlots();
	}

	public void setupSlots() {
		inventorySlots.clear();
		patternInventory = tile.patternInventories[tile.patternIndex];
		addSlotToContainer(new SlotBase(inventory, 0, 8, 26));
		for(int i = 0; i < 9; ++i) {
			for(int j = 0; j < 9; ++j) {
				addSlot(patternInventory, i*9+j, 8+j*18, 57+i*18);
			}
		}
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				addSlot(patternInventory, 81+i*3+j, 198+j*18, 111+i*18);
			}
		}
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				addSlot(patternInventory, 90+i*3+j, 198+j*18, 165+i*18);
			}
		}
		setupPlayerInventory();
	}

	public void addSlot(InventoryEncoderPattern patternInventory, int index, int x, int y) {
		if((index < 81 || index < 90 && patternInventory.recipeType.canSetOutput()) && patternInventory.recipeType.getEnabledSlots().contains(index)) {
			addSlotToContainer(new SlotFalseCopy(patternInventory, index, x, y));
		}
		else {
			addSlotToContainer(new SlotPreview(patternInventory, index, x, y));
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
