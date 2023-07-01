package thelm.packagedauto.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thelm.packagedauto.block.entity.PackagerBlockEntity;
import thelm.packagedauto.menu.PackagerMenu;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.ChangePackagingPacket;

public class PackagerScreen extends BaseScreen<PackagerMenu> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/packager.png");

	public PackagerScreen(PackagerMenu menu, Inventory inventory, Component title) {
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
		addRenderableWidget(new ButtonChangePackaging(leftPos+98, topPos+16));
	}

	@Override
	protected void renderBgAdditional(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		graphics.blit(BACKGROUND, leftPos+102, topPos+53, 176, 0, menu.blockEntity.getScaledProgress(22), 16);
		int scaledEnergy = menu.blockEntity.getScaledEnergy(40);
		graphics.blit(BACKGROUND, leftPos+10, topPos+10+40-scaledEnergy, 176, 16+40-scaledEnergy, 12, scaledEnergy);
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

	class ButtonChangePackaging extends AbstractButton {

		private static final Tooltip EXACT_TOOLTIP = Tooltip.create(PackagerBlockEntity.Mode.EXACT.getTooltip());
		private static final Tooltip DISJOINT_TOOLTIP = Tooltip.create(PackagerBlockEntity.Mode.DISJOINT.getTooltip());
		private static final Tooltip FIRST_TOOLTIP = Tooltip.create(PackagerBlockEntity.Mode.FIRST.getTooltip());

		public ButtonChangePackaging(int x, int y) {
			super(x, y, 16, 18, Component.empty());
		}

		@Override
		public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			setTooltip(currentTooltip());
			super.render(graphics, mouseX, mouseY, partialTick);
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
			super.renderWidget(graphics, mouseX, mouseY, partialTicks);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			graphics.blit(BACKGROUND, getX()+1, getY()+2, 176, 56+14*menu.blockEntity.mode.ordinal(), 14, 14);
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

		}

		@Override
		public void onPress() {
			PacketHandler.INSTANCE.sendToServer(new ChangePackagingPacket());
			menu.blockEntity.changePackagingMode();
		}

		private Tooltip currentTooltip() {
			return switch(menu.blockEntity.mode) {
			case EXACT -> EXACT_TOOLTIP;
			case DISJOINT -> DISJOINT_TOOLTIP;
			case FIRST -> FIRST_TOOLTIP;
			};
		}
	}
}
