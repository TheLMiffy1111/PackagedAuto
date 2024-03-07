package thelm.packagedauto.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity.PackageTracker;
import thelm.packagedauto.menu.UnpackagerMenu;
import thelm.packagedauto.packet.ChangeBlockingPacket;

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
	public void init() {
		clearWidgets();
		super.init();
		addRenderableWidget(new ButtonChangeBlocking(leftPos+98, topPos+16));
	}

	@Override
	protected void renderBgAdditional(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		int scaledEnergy = menu.blockEntity.getScaledEnergy(40);
		graphics.blit(BACKGROUND, leftPos+10, topPos+10+40-scaledEnergy, 176, 40-scaledEnergy, 12, scaledEnergy);
		for(int i = 0; i < menu.blockEntity.trackers.length; ++i) {
			PackageTracker tracker = menu.blockEntity.trackers[i];
			for(int j = 0; j < tracker.amount; ++j) {
				if(tracker.received.getBoolean(j)) {
					graphics.blit(BACKGROUND, leftPos+115+6*j, topPos+16+6*i, 176, 45, 6, 5);
				}
				else {
					graphics.blit(BACKGROUND, leftPos+115+6*j, topPos+16+6*i, 176, 40, 6, 5);
				}
			}
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String s = menu.blockEntity.getDisplayName().getString();
		graphics.drawString(font, s, imageWidth/2 - font.width(s)/2, 6, 0x404040, false);
		graphics.drawString(font, menu.inventory.getDisplayName().getString(), menu.getPlayerInvX(), menu.getPlayerInvY()-11, 0x404040, false);
		if(mouseX-leftPos >= 10 && mouseY-topPos >= 10 && mouseX-leftPos <= 21 && mouseY-topPos <= 49) {
			graphics.renderTooltip(font, Component.literal(menu.blockEntity.getEnergyStorage().getEnergyStored()+" / "+menu.blockEntity.getEnergyStorage().getMaxEnergyStored()+" FE"), mouseX-leftPos, mouseY-topPos);
		}
		super.renderLabels(graphics, mouseX, mouseY);
	}

	class ButtonChangeBlocking extends AbstractButton {

		private static final Tooltip TRUE_TOOLTIP = Tooltip.create(Component.translatable("block.packagedauto.unpackager.blocking.true"));
		private static final Tooltip FALSE_TOOLTIP = Tooltip.create(Component.translatable("block.packagedauto.unpackager.blocking.false"));

		public ButtonChangeBlocking(int x, int y) {
			super(x, y, 16, 18, Component.empty());
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
			setTooltip(menu.blockEntity.blocking ? TRUE_TOOLTIP : FALSE_TOOLTIP);
			super.renderWidget(graphics, mouseX, mouseY, partialTicks);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			RenderSystem.setShaderTexture(0, BACKGROUND);
			graphics.blit(BACKGROUND, getX()+1, getY()+2, 176, menu.blockEntity.blocking ? 64 : 50, 14, 14);
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

		}

		@Override
		public void onPress() {
			PacketDistributor.SERVER.with(null).send(new ChangeBlockingPacket());
		}
	}
}
