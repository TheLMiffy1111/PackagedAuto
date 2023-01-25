package thelm.packagedauto.client.gui;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import thelm.packagedauto.container.ContainerPackager;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.PacketChangePackaging;

public class GuiPackager extends GuiBase<ContainerPackager> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/packager.png");

	public GuiPackager(ContainerPackager container) {
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
		buttonList.add(new ButtonChangePackaging(0, guiLeft+98, guiTop+16));
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
		public void drawButton(Minecraft mc, int mouseX, int mouseY) {
			super.drawButton(mc, mouseX, mouseY);
			GL11.glColor4f(1, 1, 1, 1);
			mc.renderEngine.bindTexture(BACKGROUND);
			drawTexturedModalRect(xPosition+1, yPosition+2, 176, 56+14*container.tile.mode.ordinal(), 14, 14);
		}

		@Override
		public void func_146111_b(int mouseX, int mouseY) {
			drawCreativeTabHoveringText(container.tile.mode.getTooltip(), mouseX, mouseY);
		}
	}
}
