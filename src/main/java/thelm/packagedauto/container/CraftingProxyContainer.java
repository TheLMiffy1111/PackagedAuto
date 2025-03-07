package thelm.packagedauto.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import thelm.packagedauto.container.factory.PositionalTileContainerFactory;
import thelm.packagedauto.slot.SingleStackSlot;
import thelm.packagedauto.tile.CraftingProxyTile;

public class CraftingProxyContainer extends BaseContainer<CraftingProxyTile> {

	public static final ContainerType<CraftingProxyContainer> TYPE_INSTANCE = (ContainerType<CraftingProxyContainer>)IForgeContainerType.
			create(new PositionalTileContainerFactory<>(CraftingProxyContainer::new)).
			setRegistryName("packagedauto:crafting_proxy");

	public CraftingProxyContainer(int windowId, PlayerInventory playerInventory, CraftingProxyTile tile) {
		super(TYPE_INSTANCE, windowId, playerInventory, tile);
		addSlot(new SingleStackSlot(itemHandler, 0, 80, 17));
		setupPlayerInventory();
	}

	@Override
	public int getPlayerInvX() {
		return 8;
	}

	@Override
	public int getPlayerInvY() {
		return 48;
	}
}
