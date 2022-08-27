package thelm.packagedauto.menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class EmptyMenu extends AbstractContainerMenu {

	public EmptyMenu() {
		super(null, 0);
	}

	@Override
	public ItemStack quickMoveStack(Player pPlayer, int pSlot) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return false;
	}
}
