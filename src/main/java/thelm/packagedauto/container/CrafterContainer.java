package thelm.packagedauto.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.items.SlotItemHandler;
import thelm.packagedauto.container.factory.PositionalTileContainerFactory;
import thelm.packagedauto.slot.CrafterRemoveOnlySlot;
import thelm.packagedauto.slot.RemoveOnlySlot;
import thelm.packagedauto.tile.CrafterTile;

public class CrafterContainer extends BaseContainer<CrafterTile> {

	public static final ContainerType<CrafterContainer> TYPE_INSTANCE = (ContainerType<CrafterContainer>)IForgeContainerType.
			create(new PositionalTileContainerFactory<>(CrafterContainer::new)).
			setRegistryName("packagedauto:crafter");

	public CrafterContainer(int windowId, PlayerInventory playerInventory, CrafterTile tile) {
		super(TYPE_INSTANCE, windowId, playerInventory, tile);
		addSlot(new SlotItemHandler(itemHandler, 10, 8, 53));
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				addSlot(new CrafterRemoveOnlySlot(tile, i*3+j, 44+j*18, 17+i*18));
			}
		}
		addSlot(new RemoveOnlySlot(itemHandler, 9, 134, 35));
		setupPlayerInventory();
	}
}
