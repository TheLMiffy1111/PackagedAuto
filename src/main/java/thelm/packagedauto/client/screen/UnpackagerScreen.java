package thelm.packagedauto.client.screen;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity.PackageTracker;
import thelm.packagedauto.menu.UnpackagerMenu;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.ChangeBlockingPacket;
import thelm.packagedauto.network.packet.EjectTrackerPacket;
import thelm.packagedauto.network.packet.TrackerCountPacket;

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
		for(int i = 0; i < 10; ++i) {
			addRenderableWidget(new ButtonTracker(i, leftPos+115, topPos+16+6*i));
		}
		addRenderableWidget(new ButtonTrackerCount(true, leftPos+98, topPos+34));
		addRenderableWidget(new ButtonTrackerCount(false, leftPos+106, topPos+34));
	}

	@Override
	protected void renderBgAdditional(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		int scaledEnergy = menu.blockEntity.getScaledEnergy(40);
		graphics.blit(BACKGROUND, leftPos+10, topPos+10+40-scaledEnergy, 176, 40-scaledEnergy, 12, scaledEnergy);
		for(int i = 0; i < menu.blockEntity.trackers.length; ++i) {
			PackageTracker tracker = menu.blockEntity.trackers[i];
			for(int j = 0; j < 9; ++j) {
				if(j < tracker.amount) {
					if(tracker.received.getBoolean(j)) {
						graphics.blit(BACKGROUND, leftPos+115+6*j, topPos+16+6*i, 176, 45, 6, 5);
					}
					else {
						graphics.blit(BACKGROUND, leftPos+115+6*j, topPos+16+6*i, 176, 40, 6, 5);
					}
				}
				else if(i < menu.blockEntity.trackerCount) {
					graphics.blit(BACKGROUND, leftPos+115+6*j, topPos+16+6*i, 182, 45, 6, 5);
				}
				else {
					graphics.blit(BACKGROUND, leftPos+115+6*j, topPos+16+6*i, 182, 40, 6, 5);
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

		final Tooltip trueTooltip = Tooltip.create(Component.translatable("block.packagedauto.unpackager.blocking.true"));
		final Tooltip falseTooltip = Tooltip.create(Component.translatable("block.packagedauto.unpackager.blocking.false"));

		public ButtonChangeBlocking(int x, int y) {
			super(x, y, 16, 18, Component.empty());
		}

		@Override
		public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			setTooltip(menu.blockEntity.blocking ? trueTooltip : falseTooltip);
			super.render(graphics, mouseX, mouseY, partialTick);
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
			super.renderWidget(graphics, mouseX, mouseY, partialTicks);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			RenderSystem.setShaderTexture(0, BACKGROUND);
			graphics.blit(BACKGROUND, getX()+1, getY()+2, 176, menu.blockEntity.blocking ? 64 : 50, 14, 14);
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

		@Override
		public void onPress() {
			PacketHandler.INSTANCE.sendToServer(ChangeBlockingPacket.INSTANCE);
		}
	}

	class ButtonTracker extends AbstractButton {

		final int id;

		ButtonTracker(int id, int x, int y) {
			super(x, y, 54, 5, Component.empty());
			this.id = id;
			Component line0 = Component.translatable("block.packagedauto.unpackager.tracker", id);
			Component line1 = Component.translatable("block.packagedauto.unpackager.tracker.eject").withStyle(ChatFormatting.GRAY);
			setTooltip(Tooltip.create(ComponentUtils.formatList(List.of(line0, line1), Component.literal("\n"))));
		}

		@Override
		protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

		@Override
		public void onPress() {
			if(hasShiftDown()) {
				PacketHandler.INSTANCE.sendToServer(new EjectTrackerPacket(id));
			}
		}
	}

	class ButtonTrackerCount extends AbstractButton {

		final boolean decrease;

		public ButtonTrackerCount(boolean decrease, int x, int y) {
			super(x, y, 8, 18, Component.empty());
			this.decrease = decrease;
			setTooltip(Tooltip.create(Component.translatable("block.packagedauto.unpackager.tracker."+(decrease ? "decrease" : "increase"))));
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
			super.renderWidget(graphics, mouseX, mouseY, partialTicks);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			RenderSystem.setShaderTexture(0, BACKGROUND);
			graphics.blit(BACKGROUND, getX()+1, getY()+2, decrease ? 176 : 182, 78, 6, 14);
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

		@Override
		public void onPress() {
			PacketHandler.INSTANCE.sendToServer(new TrackerCountPacket(decrease));
		}
	}
}
