package thelm.packagedauto.client.gui;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import thelm.packagedauto.container.ContainerPackagerExtension;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.PacketChangePackaging;

public class GuiPackagerExtension extends GuiContainerTileBase<ContainerPackagerExtension> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/packager_extension.png");

	public GuiPackagerExtension(ContainerPackagerExtension container) {
		super(container);
	}

	@Override
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	public void initGui() {
		buttonList.clear();
		super.initGui();
		addButton(new ButtonChangePackaging(0, guiLeft+98, guiTop+16));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		drawTexturedModalRect(guiLeft+102, guiTop+53, 176, 0, container.tile.getScaledProgress(22), 16);
		int scaledEnergy = container.tile.getScaledEnergy(40);
		drawTexturedModalRect(guiLeft+10, guiTop+10+40-scaledEnergy, 176, 16+40-scaledEnergy, 12, scaledEnergy);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		String s = container.inventory.getDisplayName().getUnformattedText();
		fontRenderer.drawString(s, xSize/2 - fontRenderer.getStringWidth(s)/2, 6, 0x404040);
		fontRenderer.drawString(container.playerInventory.getDisplayName().getUnformattedText(), container.getPlayerInvX(), container.getPlayerInvY()-11, 0x404040);
		if(mouseX-guiLeft >= 10 && mouseY-guiTop >= 10 && mouseX-guiLeft <= 21 && mouseY-guiTop <= 49) {
			drawHoveringText(container.tile.getEnergyStorage().getEnergyStored()+" / "+container.tile.getEnergyStorage().getMaxEnergyStored()+" FE", mouseX-guiLeft, mouseY-guiTop);
		}
		for(GuiButton guibutton : buttonList) {
			if(guibutton.isMouseOver()) {
				guibutton.drawButtonForegroundLayer(mouseX-guiLeft, mouseY-guiTop);
				break;
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if(button instanceof ButtonChangePackaging) {
			PacketHandler.INSTANCE.sendToServer(new PacketChangePackaging());
			container.tile.changePackagingMode();
		}
	}

	class ButtonChangePackaging extends GuiButton {

		ButtonChangePackaging(int buttonId, int x, int y) {
			super(buttonId, x, y, 16, 18, "");
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
			super.drawButton(mc, mouseX, mouseY, partialTicks);
			GlStateManager.color(1, 1, 1, 1);
			mc.renderEngine.bindTexture(BACKGROUND);
			drawTexturedModalRect(x+1, y+2, 176, 56+14*container.tile.mode.ordinal(), 14, 14);
		}

		@Override
		public void drawButtonForegroundLayer(int mouseX, int mouseY) {
			drawHoveringText(container.tile.mode.getTooltip(), mouseX, mouseY);
		}
	}
}
