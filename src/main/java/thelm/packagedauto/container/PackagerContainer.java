package thelm.packagedauto.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.items.SlotItemHandler;
import thelm.packagedauto.container.factory.PositionalTileContainerFactory;
import thelm.packagedauto.slot.RemoveOnlySlot;
import thelm.packagedauto.slot.SingleStackSlot;
import thelm.packagedauto.tile.PackagerTile;

public class PackagerContainer extends BaseContainer<PackagerTile> {

	public static final ContainerType<PackagerContainer> TYPE_INSTANCE = (ContainerType<PackagerContainer>)IForgeContainerType.
			create(new PositionalTileContainerFactory<>(PackagerContainer::new)).
			setRegistryName("packagedauto:packager");

	public PackagerContainer(int windowId, PlayerInventory playerInventory, PackagerTile tile) {
		super(TYPE_INSTANCE, windowId, playerInventory, tile);
		addSlot(new SingleStackSlot(itemHandler, 10, 134, 17));
		addSlot(new SlotItemHandler(itemHandler, 11, 8, 53));
		for(int i = 0; i < 3; ++i)  {
			for(int j = 0; j < 3; ++j) {
				addSlot(new SlotItemHandler(itemHandler, j+i*3, 44+j*18, 17+i*18));
			}
		}
		addSlot(new RemoveOnlySlot(itemHandler, 9, 134, 53));
		setupPlayerInventory();
	}
}
