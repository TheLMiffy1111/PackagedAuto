package thelm.packagedauto.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import thelm.packagedauto.container.BaseContainer;
import thelm.packagedauto.slot.FalseCopySlot;

public abstract class BaseScreen<C extends BaseContainer<?>> extends ContainerScreen<C> {

	public final C container;

	public BaseScreen(C container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
		this.container = container;
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		renderHoveredTooltip(matrixStack, mouseX, mouseY);
	}

	protected abstract ResourceLocation getBackgroundTexture();

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1F, 1F, 1F, 1F);
		minecraft.getTextureManager().bindTexture(getBackgroundTexture());
		if(xSize > 256 || ySize > 256) {
			blit(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize, 512, 512);
		}
		else {
			blit(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);
		}
	}

	@Override
	protected void handleMouseClick(Slot slot, int slotId, int mouseButton, ClickType type) {
		boolean valid = type != ClickType.QUICK_MOVE && minecraft.player.inventory.getItemStack().isEmpty();
		if(valid && slot instanceof FalseCopySlot && slot.isEnabled()) {
			if(!slot.getStack().isEmpty()) {
				minecraft.displayGuiScreen(new AmountSpecifyingScreen(
						this, minecraft.player.inventory,
						slot.slotNumber, slot.getStack(),
						Math.min(slot.getSlotStackLimit(), slot.getStack().getMaxStackSize())));
			}
		}
		else {
			super.handleMouseClick(slot, slotId, mouseButton, type);
		}
	}
}
