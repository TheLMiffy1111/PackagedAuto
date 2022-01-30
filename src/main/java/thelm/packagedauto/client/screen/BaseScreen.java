package thelm.packagedauto.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thelm.packagedauto.menu.BaseMenu;

public abstract class BaseScreen<C extends BaseMenu<?>> extends AbstractContainerScreen<C> {

	public final C menu;

	public BaseScreen(C menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.menu = menu;
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(poseStack);
		super.render(poseStack, mouseX, mouseY, partialTicks);
		renderTooltip(poseStack, mouseX, mouseY);
	}

	protected abstract ResourceLocation getBackgroundTexture();

	@Override
	protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		RenderSystem.setShaderTexture(0, getBackgroundTexture());
		if(imageWidth > 256 || imageHeight > 256) {
			blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight, 512, 512);
		}
		else {
			blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		}
	}
}
