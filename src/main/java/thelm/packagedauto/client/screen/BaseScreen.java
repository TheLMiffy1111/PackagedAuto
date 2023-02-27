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

	public final C menu;

	public BaseScreen(C container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
		this.menu = container;
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		renderTooltip(matrixStack, mouseX, mouseY);
	}

	protected abstract ResourceLocation getBackgroundTexture();

	@Override
	protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1F, 1F, 1F, 1F);
		minecraft.getTextureManager().bind(getBackgroundTexture());
		if(imageWidth > 256 || imageHeight > 256) {
			blit(matrixStack, leftPos, topPos, 0, 0, imageWidth, imageHeight, 512, 512);
		}
		else {
			blit(matrixStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		}
	}

	@Override
	protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
		boolean valid = type != ClickType.QUICK_MOVE && minecraft.player.inventory.getCarried().isEmpty();
		if(valid && slot instanceof FalseCopySlot && slot.isActive()) {
			if(!slot.getItem().isEmpty()) {
				minecraft.setScreen(new AmountSpecifyingScreen(
						this, minecraft.player.inventory,
						slot.index, slot.getItem(),
						Math.min(slot.getMaxStackSize(), slot.getItem().getMaxStackSize())));
			}
		}
		else {
			super.slotClicked(slot, slotId, mouseButton, type);
		}
	}
}
