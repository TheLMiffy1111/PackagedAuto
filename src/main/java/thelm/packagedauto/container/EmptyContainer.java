package thelm.packagedauto.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;

public class EmptyContainer extends Container {

	public EmptyContainer() {
		super(null, 0);
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return false;
	}
}
