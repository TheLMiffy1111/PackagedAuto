package thelm.packagedauto.client.gui;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import thelm.packagedauto.container.ContainerUnpackager;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.PacketChangeBlocking;
import thelm.packagedauto.tile.TileUnpackager.PackageTracker;

public class GuiUnpackager extends GuiBase<ContainerUnpackager> {

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
		buttonList.add(new GuiButtonChangeBlocking(0, guiLeft+98, guiTop+16));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		int scaledEnergy = container.tile.getScaledEnergy(40);
		drawTexturedModalRect(guiLeft+10, guiTop+10+40-scaledEnergy, 176, 40-scaledEnergy, 12, scaledEnergy);
		for(int i = 0; i < container.tile.trackers.length; ++i) {
			PackageTracker tracker = container.tile.trackers[i];
			for(int j = 0; j < tracker.amount; ++j) {
				if(tracker.received.get(j)) {
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
		String s = container.inventory.getInventoryName();
		fontRendererObj.drawString(s, xSize/2 - fontRendererObj.getStringWidth(s)/2, 6, 0x404040);
		fontRendererObj.drawString(StatCollector.translateToLocal(container.playerInventory.getInventoryName()), container.getPlayerInvX(), container.getPlayerInvY()-11, 0x404040);
		if(mouseX-guiLeft >= 10 && mouseY-guiTop >= 10 && mouseX-guiLeft <= 21 && mouseY-guiTop <= 49) {
			drawCreativeTabHoveringText(container.tile.getEnergyStorage().getEnergyStored()+" / "+container.tile.getEnergyStorage().getMaxEnergyStored()+" RF", mouseX-guiLeft, mouseY-guiTop);
		}
		for(GuiButton guibutton : (List<GuiButton>)buttonList) {
			if(guibutton.func_146115_a()) {
				guibutton.func_146111_b(mouseX-guiLeft, mouseY-guiTop);
				break;
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if(button instanceof GuiButtonChangeBlocking) {
			PacketHandler.INSTANCE.sendToServer(new PacketChangeBlocking());
			container.tile.changeBlockingMode();
		}
	}

	class GuiButtonChangeBlocking extends GuiButton {

		public GuiButtonChangeBlocking(int buttonId, int x, int y) {
			super(buttonId, x, y, 16, 18, "");
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY) {
			super.drawButton(mc, mouseX, mouseY);
			GL11.glColor4f(1, 1, 1, 1);
			mc.renderEngine.bindTexture(BACKGROUND);
			drawTexturedModalRect(xPosition+1, yPosition+2, 176, container.tile.blocking ? 64 : 50, 14, 14);
		}

		@Override
		public void func_146111_b(int mouseX, int mouseY) {
			drawCreativeTabHoveringText(StatCollector.translateToLocal("tile.packagedauto.unpackager.blocking."+container.tile.blocking), mouseX, mouseY);
		}
	}
}
