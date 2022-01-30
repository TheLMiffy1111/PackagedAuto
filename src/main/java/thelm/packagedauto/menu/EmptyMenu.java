package thelm.packagedauto.menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class EmptyMenu extends AbstractContainerMenu {

	public EmptyMenu() {
		super(null, 0);
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return false;
	}
}
