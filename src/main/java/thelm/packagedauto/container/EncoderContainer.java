package thelm.packagedauto.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.items.SlotItemHandler;
import thelm.packagedauto.container.factory.PositionalTileContainerFactory;
import thelm.packagedauto.inventory.EncoderPatternItemHandler;
import thelm.packagedauto.slot.FalseCopySlot;
import thelm.packagedauto.slot.PreviewSlot;
import thelm.packagedauto.tile.EncoderTile;

public class EncoderContainer extends BaseContainer<EncoderTile> {

	public static final ContainerType<EncoderContainer> TYPE_INSTANCE = (ContainerType<EncoderContainer>)IForgeContainerType.
			create(new PositionalTileContainerFactory<>(EncoderContainer::new)).
			setRegistryName("packagedauto:encoder");

	public EncoderPatternItemHandler patternItemHandler;

	public EncoderContainer(int windowId, PlayerInventory playerInventory, EncoderTile tile) {
		super(TYPE_INSTANCE, windowId, playerInventory, tile);
		setupSlots();
	}

	public void setupSlots() {
		inventorySlots.clear();
		patternItemHandler = tile.patternItemHandlers[tile.patternIndex];
		addSlot(new SlotItemHandler(itemHandler, 0, 8, 26));
		for(int i = 0; i < 9; ++i) {
			for(int j = 0; j < 9; ++j) {
				addSlot(patternItemHandler, i*9+j, 8+j*18, 57+i*18);
			}
		}
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				addSlot(patternItemHandler, 81+i*3+j, 198+j*18, 111+i*18);
			}
		}
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
