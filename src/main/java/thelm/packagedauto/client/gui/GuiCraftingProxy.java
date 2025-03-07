package thelm.packagedauto.client.gui;

import net.minecraft.util.ResourceLocation;
import thelm.packagedauto.container.ContainerCraftingProxy;

public class GuiCraftingProxy extends GuiContainerTileBase<ContainerCraftingProxy> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/crafting_proxy.png");

	public GuiCraftingProxy(ContainerCraftingProxy container) {
		super(container);
		xSize = 176;
		ySize = 130;
	}

	@Override
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		String s = container.inventory.getDisplayName().getUnformattedText();
		fontRenderer.drawString(s, xSize/2 - fontRenderer.getStringWidth(s)/2, 6, 0x404040);
		fontRenderer.drawString(container.playerInventory.getDisplayName().getUnformattedText(), container.getPlayerInvX(), container.getPlayerInvY()-11, 0x404040);
	}
}
