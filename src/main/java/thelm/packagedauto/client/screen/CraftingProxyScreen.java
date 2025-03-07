package thelm.packagedauto.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thelm.packagedauto.menu.CraftingProxyMenu;

public class CraftingProxyScreen extends BaseScreen<CraftingProxyMenu> {

	public static final ResourceLocation BACKGROUND = ResourceLocation.parse("packagedauto:textures/gui/crafting_proxy.png");

	public CraftingProxyScreen(CraftingProxyMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		imageWidth = 176;
		imageHeight = 130;
	}

	@Override
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String s = menu.blockEntity.getDisplayName().getString();
		graphics.drawString(font, s, imageWidth/2 - font.width(s)/2, 6, 0x404040, false);
		graphics.drawString(font, menu.inventory.getDisplayName().getString(), menu.getPlayerInvX(), menu.getPlayerInvY()-11, 0x404040, false);
		super.renderLabels(graphics, mouseX, mouseY);
	}
}
