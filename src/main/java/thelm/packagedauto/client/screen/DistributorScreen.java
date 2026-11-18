package thelm.packagedauto.client.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import thelm.packagedauto.menu.DistributorMenu;

public class DistributorScreen extends BaseScreen<DistributorMenu> {

	public static final Identifier BACKGROUND = Identifier.parse("packagedauto:textures/gui/distributor.png");

	public DistributorScreen(DistributorMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, 274);
	}

	@Override
	protected Identifier getBackgroundTexture() {
		return BACKGROUND;
	}
}
