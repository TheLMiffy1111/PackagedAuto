package thelm.packagedauto.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.items.SlotItemHandler;
import thelm.packagedauto.container.factory.PositionalTileContainerFactory;
import thelm.packagedauto.slot.SingleStackSlot;
import thelm.packagedauto.tile.UnpackagerTile;

public class UnpackagerContainer extends BaseContainer<UnpackagerTile> {

	public static final ContainerType<UnpackagerContainer> TYPE_INSTANCE = (ContainerType<UnpackagerContainer>)IForgeContainerType.
			create(new PositionalTileContainerFactory<>(UnpackagerContainer::new)).
			setRegistryName("packagedauto:unpackager");

	public UnpackagerContainer(int windowId, PlayerInventory playerInventory, UnpackagerTile tile) {
		super(TYPE_INSTANCE, windowId, playerInventory, tile);
		addSlot(new SingleStackSlot(itemHandler, 9, 26, 17));
		addSlot(new SlotItemHandler(itemHandler, 10, 8, 53));
		for(int i = 0; i < 3; ++i)  {
			for(int j = 0; j < 3; ++j) {
				addSlot(new SlotItemHandler(itemHandler, j+i*3, 44+j*18, 17+i*18));
			}
		}
		setupPlayerInventory();
	}
}
