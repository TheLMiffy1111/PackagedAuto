package thelm.packagedauto.client.gui;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import thelm.packagedauto.container.ContainerBase;
import thelm.packagedauto.slot.SlotFalseCopy;

public abstract class GuiBase<CONTAINER extends ContainerBase<?>> extends GuiContainer {

	public final CONTAINER container;

	public GuiBase(CONTAINER container) {
		super(container);
		this.container = container;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	protected abstract ResourceLocation getBackgroundTexture();

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1, 1, 1, 1);
		mc.getTextureManager().bindTexture(getBackgroundTexture());
		if(xSize > 256 || ySize > 256) {
			func_146110_a(guiLeft, guiTop, 0, 0, xSize, ySize, 512, 512);
		}
		else {
			drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		}
	}

	@Override
	protected void handleMouseClick(Slot slot, int slotId, int mouseButton, int type) {
		boolean valid = type != 1 && mc.thePlayer.inventory.getItemStack() == null;
		if(valid && slot instanceof SlotFalseCopy && slot.func_111238_b()) {
			if(slot.getStack() != null) {
				mc.displayGuiScreen(new GuiAmountSpecifying(
						this, mc.thePlayer.inventory,
						slot.slotNumber, slot.getStack(),
						Math.min(slot.getSlotStackLimit(), slot.getStack().getMaxStackSize())));
			}
		}
		else {
			super.handleMouseClick(slot, slotId, mouseButton, type);
		}
	}

	@Override
	protected void drawHoveringText(List tooltip, int x, int y, FontRenderer font) {
		super.drawHoveringText(tooltip, x, y, font);
		RenderHelper.enableGUIStandardItemLighting();
	}

	public int getLeft() {
		return guiLeft;
	}

	public int getTop() {
		return guiTop;
	}
}
