package thelm.packagedauto.client.gui;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import thelm.packagedauto.container.ContainerCrafter;

public class GuiCrafter extends GuiBase<ContainerCrafter> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/crafter.png");

	public GuiCrafter(ContainerCrafter container) {
		super(container);
	}

	@Override
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		drawTexturedModalRect(guiLeft+102, guiTop+35, 176, 0, container.tile.getScaledProgress(22), 16);
		int scaledEnergy = container.tile.getScaledEnergy(40);
		drawTexturedModalRect(guiLeft+10, guiTop+10+40-scaledEnergy, 176, 16+40-scaledEnergy, 12, scaledEnergy);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		String s = container.inventory.getInventoryName();
		fontRendererObj.drawString(s, xSize/2 - fontRendererObj.getStringWidth(s)/2, 6, 0x404040);
		fontRendererObj.drawString(StatCollector.translateToLocal(container.playerInventory.getInventoryName()), container.getPlayerInvX(), container.getPlayerInvY()-11, 0x404040);
		if(mouseX-guiLeft >= 10 && mouseY-guiTop >= 10 && mouseX-guiLeft <= 21 && mouseY-guiTop <= 49) {
			drawCreativeTabHoveringText(container.tile.getEnergyStorage().getEnergyStored()+" / "+container.tile.getEnergyStorage().getMaxEnergyStored()+" RF", mouseX-guiLeft, mouseY-guiTop);
		}
	}
}
