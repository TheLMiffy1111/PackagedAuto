package thelm.packagedauto.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import thelm.packagedauto.container.ContainerTileBase;
import thelm.packagedauto.slot.SlotFalseCopy;

public abstract class GuiContainerTileBase<CONTAINER extends ContainerTileBase<?>> extends GuiContainer {

	public final CONTAINER container;

	public GuiContainerTileBase(CONTAINER container) {
		super(container);
		this.container = container;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}

	protected abstract ResourceLocation getBackgroundTexture();

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(getBackgroundTexture());
		if(xSize > 256 || ySize > 256) {
			drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, xSize, ySize, 512, 512);
		}
		else {
			drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		}
	}

	@Override
	protected void handleMouseClick(Slot slot, int slotId, int mouseButton, ClickType type) {
		if(mouseButton == 0 &&
				type != ClickType.QUICK_MOVE &&
				mc.player.inventory.getItemStack().isEmpty() &&
				slot instanceof SlotFalseCopy &&
				slot.isEnabled() &&
				!slot.getStack().isEmpty()) {
			mc.displayGuiScreen(new GuiItemAmountSpecifying(
					this, mc.player.inventory, slot.slotNumber, slot.getStack(), getItemAmountSpecificationLimit(slot)));
		}
		else {
			super.handleMouseClick(slot, slotId, mouseButton, type);
		}
	}

	public int getItemAmountSpecificationLimit(Slot slot) {
		return Math.min(slot.getSlotStackLimit(), slot.getStack().getMaxStackSize());
	}
}
