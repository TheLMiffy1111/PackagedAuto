package thelm.packagedauto.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.items.SlotItemHandler;
import thelm.packagedauto.container.factory.PositionalTileContainerFactory;
import thelm.packagedauto.slot.PreviewSlot;
import thelm.packagedauto.slot.RemoveOnlySlot;
import thelm.packagedauto.tile.PackagerExtensionTile;

public class PackagerExtensionContainer extends BaseContainer<PackagerExtensionTile> {

	public static final ContainerType<PackagerExtensionContainer> TYPE_INSTANCE = (ContainerType<PackagerExtensionContainer>)IForgeContainerType.
			create(new PositionalTileContainerFactory<>(PackagerExtensionContainer::new)).
			setRegistryName("packagedauto:packager_extension");

	public PackagerExtensionContainer(int windowId, PlayerInventory playerInventory, PackagerExtensionTile tile) {
		super(TYPE_INSTANCE, windowId, playerInventory, tile);
		addSlot(new SlotItemHandler(itemHandler, 10, 8, 53));
		for(int i = 0; i < 3; ++i)  {
			for(int j = 0; j < 3; ++j) {
				addSlot(new SlotItemHandler(itemHandler, j+i*3, 44+j*18, 17+i*18));
			}
		}
		addSlot(new RemoveOnlySlot(itemHandler, 9, 134, 53));
		addSlot(new PreviewSlot(tile.listStackItemHandler, 0, 134, 17));
		setupPlayerInventory();
	}
}
