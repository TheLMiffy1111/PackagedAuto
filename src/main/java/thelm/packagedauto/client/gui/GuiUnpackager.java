package thelm.packagedauto.client.gui;

import net.minecraft.util.ResourceLocation;
import thelm.packagedauto.container.ContainerUnpackager;
import thelm.packagedauto.tile.TileUnpackager.PackageTracker;

public class GuiUnpackager extends GuiContainerTileBase<ContainerUnpackager> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/unpackager.png");

	public GuiUnpackager(ContainerUnpackager container) {
		super(container);
	}

	@Override
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		int scaledEnergy = container.tile.getScaledEnergy(40);
		drawTexturedModalRect(guiLeft+10, guiTop+10+40-scaledEnergy, 176, 40-scaledEnergy, 12, scaledEnergy);
		for(int i = 0; i < container.tile.trackers.length; ++i) {
			PackageTracker tracker = container.tile.trackers[i];
			for(int j = 0; j < tracker.amount; ++j) {
				if(tracker.received.getBoolean(j)) {
					drawTexturedModalRect(guiLeft+115+6*j, guiTop+16+6*i, 176, 45, 6, 5);
				}
				else {
					drawTexturedModalRect(guiLeft+115+6*j, guiTop+16+6*i, 176, 40, 6, 5);
				}
			}
		}
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
	}
}
