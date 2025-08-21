package thelm.packagedauto.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.items.SlotItemHandler;
import thelm.packagedauto.block.entity.EncoderBlockEntity;
import thelm.packagedauto.inventory.EncoderPatternItemHandler;
import thelm.packagedauto.slot.FalseCopySlot;
import thelm.packagedauto.slot.PreviewSlot;

public class EncoderMenu extends BaseMenu<EncoderBlockEntity> {

	public EncoderPatternItemHandler patternItemHandler;

	public EncoderMenu(int windowId, Inventory playerInventory, EncoderBlockEntity blockEntity) {
		super(PackagedAutoMenus.ENCODER.get(), windowId, playerInventory, blockEntity);
		setupSlots(true);
	}

	public void setupSlots() {
		setupSlots(false);
	}

	protected void setupSlots(boolean init) {
		patternItemHandler = blockEntity.patternItemHandlers[blockEntity.patternIndex];
		if(init) {
			addSlot(new SlotItemHandler(itemHandler, 0, 8, 26));
		}
		for(int i = 0; i < 9; ++i) {
			for(int j = 0; j < 9; ++j) {
				addPatternSlot(patternItemHandler, i*9+j, 8+j*18, 57+i*18, init);
			}
		}
		addPatternSlot(patternItemHandler, 81, 216, 129, init);
		addPatternSlot(patternItemHandler, 82, 198, 111, init);
		addPatternSlot(patternItemHandler, 83, 216, 111, init);
		addPatternSlot(patternItemHandler, 84, 234, 111, init);
		addPatternSlot(patternItemHandler, 85, 198, 129, init);
		addPatternSlot(patternItemHandler, 86, 234, 129, init);
		addPatternSlot(patternItemHandler, 87, 198, 147, init);
		addPatternSlot(patternItemHandler, 88, 216, 147, init);
		addPatternSlot(patternItemHandler, 89, 234, 147, init);
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				addPatternSlot(patternItemHandler, 90+i*3+j, 198+j*18, 165+i*18, init);
			}
		}
		if(init) {
			setupPlayerInventory();
		}
	}

	protected void addPatternSlot(EncoderPatternItemHandler patternItemHandler, int index, int x, int y, boolean init) {
		Slot slot;
		int slotIndex = index + 1;
		if((index < 81 || index < 90 && patternItemHandler.recipeType.canSetOutput()) && patternItemHandler.recipeType.getEnabledSlots().contains(index)) {
			slot = new FalseCopySlot(patternItemHandler, index, x, y);
		}
		else {
			slot = new PreviewSlot(patternItemHandler, index, x, y);
		}
		slot.index = slotIndex;
		if(init) {
			addSlot(slot);
		}
		else {
			slots.set(slotIndex, slot);
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
