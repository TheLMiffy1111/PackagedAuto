package thelm.packagedauto.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.items.SlotItemHandler;
import thelm.packagedauto.block.entity.EncoderBlockEntity;
import thelm.packagedauto.inventory.EncoderPatternItemHandler;
import thelm.packagedauto.menu.factory.PositionalBlockEntityMenuFactory;
import thelm.packagedauto.slot.FalseCopySlot;
import thelm.packagedauto.slot.PreviewSlot;

public class EncoderMenu extends BaseMenu<EncoderBlockEntity> {

	public static final MenuType<EncoderMenu> TYPE_INSTANCE = IMenuTypeExtension.create(new PositionalBlockEntityMenuFactory<>(EncoderMenu::new));

	public EncoderPatternItemHandler patternItemHandler;

	public EncoderMenu(int windowId, Inventory playerInventory, EncoderBlockEntity blockEntity) {
		super(TYPE_INSTANCE, windowId, playerInventory, blockEntity);
		setupSlots();
	}

	public void setupSlots() {
		slots.clear();
		patternItemHandler = blockEntity.patternItemHandlers[blockEntity.patternIndex];
		addSlot(new SlotItemHandler(itemHandler, 0, 8, 26));
		for(int i = 0; i < 9; ++i) {
			for(int j = 0; j < 9; ++j) {
				addSlot(patternItemHandler, i*9+j, 8+j*18, 57+i*18);
			}
		}
		addSlot(patternItemHandler, 81, 216, 129);
		addSlot(patternItemHandler, 82, 198, 111);
		addSlot(patternItemHandler, 83, 216, 111);
		addSlot(patternItemHandler, 84, 234, 111);
		addSlot(patternItemHandler, 85, 198, 129);
		addSlot(patternItemHandler, 86, 234, 129);
		addSlot(patternItemHandler, 87, 198, 147);
		addSlot(patternItemHandler, 88, 216, 147);
		addSlot(patternItemHandler, 89, 234, 147);
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				addSlot(patternItemHandler, 90+i*3+j, 198+j*18, 165+i*18);
			}
		}
		setupPlayerInventory();
	}

	public void addSlot(EncoderPatternItemHandler patternItemHandler, int index, int x, int y) {
		if((index < 81 || index < 90 && patternItemHandler.recipeType.canSetOutput()) && patternItemHandler.recipeType.getEnabledSlots().contains(index)) {
			addSlot(new FalseCopySlot(patternItemHandler, index, x, y));
		}
		else {
			addSlot(new PreviewSlot(patternItemHandler, index, x, y));
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
