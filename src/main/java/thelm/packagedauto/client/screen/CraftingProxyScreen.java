package thelm.packagedauto.client.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import thelm.packagedauto.menu.CraftingProxyMenu;

public class CraftingProxyScreen extends BaseScreen<CraftingProxyMenu> {

	public static final Identifier BACKGROUND = Identifier.parse("packagedauto:textures/gui/crafting_proxy.png");

	public CraftingProxyScreen(CraftingProxyMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, 130);
	}

	@Override
	protected Identifier getBackgroundTexture() {
		return BACKGROUND;
	}
}
