package thelm.packagedauto.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity.PackageTracker;
import thelm.packagedauto.menu.UnpackagerMenu;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.ChangeBlockingPacket;

public class UnpackagerScreen extends BaseScreen<UnpackagerMenu> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/unpackager.png");

	public UnpackagerScreen(UnpackagerMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	protected void renderBgAdditional(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
		int scaledEnergy = menu.blockEntity.getScaledEnergy(40);
		blit(poseStack, leftPos+10, topPos+10+40-scaledEnergy, 176, 40-scaledEnergy, 12, scaledEnergy);
		for(int i = 0; i < menu.blockEntity.trackers.length; ++i) {
			PackageTracker tracker = menu.blockEntity.trackers[i];
			for(int j = 0; j < tracker.amount; ++j) {
				if(tracker.received.getBoolean(j)) {
					blit(poseStack, leftPos+115+6*j, topPos+16+6*i, 176, 45, 6, 5);
				}
				else {
					blit(poseStack, leftPos+115+6*j, topPos+16+6*i, 176, 40, 6, 5);
				}
			}
		}
	}

	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
		String s = menu.blockEntity.getDisplayName().getString();
		font.draw(poseStack, s, imageWidth/2 - font.width(s)/2, 6, 0x404040);
		font.draw(poseStack, menu.inventory.getDisplayName().getString(), menu.getPlayerInvX(), menu.getPlayerInvY()-11, 0x404040);
		if(mouseX-leftPos >= 10 && mouseY-topPos >= 10 && mouseX-leftPos <= 21 && mouseY-topPos <= 49) {
			renderTooltip(poseStack, new TextComponent(menu.blockEntity.getEnergyStorage().getEnergyStored()+" / "+menu.blockEntity.getEnergyStorage().getMaxEnergyStored()+" FE"), mouseX-leftPos, mouseY-topPos);
		}
		for(GuiEventListener child : children()) {
			if(child.isMouseOver(mouseX, mouseY) && child instanceof AbstractWidget button) {
				button.renderToolTip(poseStack, mouseX-leftPos, mouseY-topPos);
				break;
			}
		}
		super.renderLabels(poseStack, mouseX, mouseY);
	}

	@Override
	public void init() {
		clearWidgets();
		super.init();
		addRenderableWidget(new ButtonChangeBlocking(leftPos+98, topPos+16));
	}

	class ButtonChangeBlocking extends AbstractWidget {

		public ButtonChangeBlocking(int x, int y) {
			super(x, y, 16, 18, TextComponent.EMPTY);
		}

		@Override
		public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
			super.renderButton(poseStack, mouseX, mouseY, partialTicks);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			RenderSystem.setShaderTexture(0, BACKGROUND);
			blit(poseStack, x+1, y+2, 176, menu.blockEntity.blocking ? 64 : 50, 14, 14);
		}

		@Override
		public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
			renderTooltip(poseStack, new TranslatableComponent("block.packagedauto.unpackager.blocking."+menu.blockEntity.blocking), mouseX, mouseY);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			PacketHandler.INSTANCE.sendToServer(new ChangeBlockingPacket());
			menu.blockEntity.changeBlockingMode();
		}

		@Override
		public void updateNarration(NarrationElementOutput narrationElementOutput) {

		}
	}
}
