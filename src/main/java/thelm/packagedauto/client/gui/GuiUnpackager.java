package thelm.packagedauto.client.gui;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import thelm.packagedauto.container.ContainerUnpackager;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.PacketChangeBlocking;
import thelm.packagedauto.network.packet.PacketTrackerCount;
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
	public void initGui() {
		buttonList.clear();
		super.initGui();
		addButton(new GuiButtonChangeBlocking(0, guiLeft+98, guiTop+16));
		addButton(new GuiButtonTrackerCount(0, guiLeft+98, guiTop+34));
		addButton(new GuiButtonTrackerCount(1, guiLeft+106, guiTop+34));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		int scaledEnergy = container.tile.getScaledEnergy(40);
		drawTexturedModalRect(guiLeft+10, guiTop+10+40-scaledEnergy, 176, 40-scaledEnergy, 12, scaledEnergy);
		for(int i = 0; i < container.tile.trackers.length; ++i) {
			PackageTracker tracker = container.tile.trackers[i];
			for(int j = 0; j < 9; ++j) {
				if(j < tracker.amount) {
					if(tracker.received.getBoolean(j)) {
						drawTexturedModalRect(guiLeft+115+6*j, guiTop+16+6*i, 176, 45, 6, 5);
					}
					else {
						drawTexturedModalRect(guiLeft+115+6*j, guiTop+16+6*i, 176, 40, 6, 5);
					}
				}
				else if(i < container.tile.trackerCount) {
					drawTexturedModalRect(guiLeft+115+6*j, guiTop+16+6*i, 182, 45, 6, 5);
				}
				else {
					drawTexturedModalRect(guiLeft+115+6*j, guiTop+16+6*i, 182, 40, 6, 5);
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
		for(GuiButton guibutton : buttonList) {
			if(guibutton.isMouseOver()) {
				guibutton.drawButtonForegroundLayer(mouseX-guiLeft, mouseY-guiTop);
				break;
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if(button instanceof GuiButtonChangeBlocking) {
			PacketHandler.INSTANCE.sendToServer(new PacketChangeBlocking());
		}
		if(button instanceof GuiButtonTrackerCount) {
			PacketHandler.INSTANCE.sendToServer(new PacketTrackerCount(button.id == 0));
		}
	}

	class GuiButtonChangeBlocking extends GuiButton {

		public GuiButtonChangeBlocking(int buttonId, int x, int y) {
			super(buttonId, x, y, 16, 18, "");
		}

		@Override
		protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
			GlStateManager.color(1, 1, 1, 1);
			mc.renderEngine.bindTexture(BACKGROUND);
			drawTexturedModalRect(x+1, y+2, 176, container.tile.blocking ? 64 : 50, 14, 14);
		}

		@Override
		public void drawButtonForegroundLayer(int mouseX, int mouseY) {
			drawHoveringText(I18n.translateToLocal("tile.packagedauto.unpackager.blocking."+container.tile.blocking), mouseX, mouseY);
		}
	}

	class GuiButtonTrackerCount extends GuiButton {

		public GuiButtonTrackerCount(int buttonId, int x, int y) {
			super(buttonId, x, y, 8, 18, "");
		}

		@Override
		protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
			GlStateManager.color(1, 1, 1, 1);
			mc.renderEngine.bindTexture(BACKGROUND);
			drawTexturedModalRect(x+1, y+2, id == 0 ? 176 : 182, 78, 6, 14);
		}

		@Override
		public void drawButtonForegroundLayer(int mouseX, int mouseY) {
			drawHoveringText(I18n.translateToLocal("tile.packagedauto.unpackager.tracker."+(id == 0 ? "decrease" : "increase")), mouseX, mouseY);
		}
	}
}
