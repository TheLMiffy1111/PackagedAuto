package thelm.packagedauto.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thelm.packagedauto.block.entity.PackagerBlockEntity;
import thelm.packagedauto.menu.PackagerExtensionMenu;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.ChangePackagingPacket;

public class PackagerExtensionScreen extends BaseScreen<PackagerExtensionMenu> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/packager_extension.png");

	public PackagerExtensionScreen(PackagerExtensionMenu menu, Inventory inventory, Component title) {
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
	protected void renderBgAdditional(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
		blit(poseStack, leftPos+102, topPos+53, 176, 0, menu.blockEntity.getScaledProgress(22), 16);
		int scaledEnergy = menu.blockEntity.getScaledEnergy(40);
		blit(poseStack, leftPos+10, topPos+10+40-scaledEnergy, 176, 16+40-scaledEnergy, 12, scaledEnergy);
	}

	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
		String s = menu.blockEntity.getDisplayName().getString();
		font.draw(poseStack, s, imageWidth/2 - font.width(s)/2, 6, 0x404040);
		font.draw(poseStack, menu.inventory.getDisplayName().getString(), menu.getPlayerInvX(), menu.getPlayerInvY()-11, 0x404040);
		if(mouseX-leftPos >= 10 && mouseY-topPos >= 10 && mouseX-leftPos <= 21 && mouseY-topPos <= 49) {
			renderTooltip(poseStack, Component.literal(menu.blockEntity.getEnergyStorage().getEnergyStored()+" / "+menu.blockEntity.getEnergyStorage().getMaxEnergyStored()+" FE"), mouseX-leftPos, mouseY-topPos);
		}
		super.renderLabels(poseStack, mouseX, mouseY);
	}

	class ButtonChangePackaging extends AbstractWidget {

		private static final Tooltip EXACT_TOOLTIP = Tooltip.create(PackagerBlockEntity.Mode.EXACT.getTooltip());
		private static final Tooltip DISJOINT_TOOLTIP = Tooltip.create(PackagerBlockEntity.Mode.DISJOINT.getTooltip());
		private static final Tooltip FIRST_TOOLTIP = Tooltip.create(PackagerBlockEntity.Mode.FIRST.getTooltip());

		public ButtonChangePackaging(int x, int y) {
			super(x, y, 16, 18, Component.empty());
		}

		@Override
		public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
			setTooltip(getTooltip());
			super.render(poseStack, mouseX, mouseY, partialTick);
		}

		@Override
		public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
			super.renderButton(poseStack, mouseX, mouseY, partialTicks);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			RenderSystem.setShaderTexture(0, BACKGROUND);
			blit(poseStack, getX()+1, getY()+2, 176, 56+14*menu.blockEntity.mode.ordinal(), 14, 14);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			PacketHandler.INSTANCE.sendToServer(new ChangePackagingPacket());
			menu.blockEntity.changePackagingMode();
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

		}

		private Tooltip getTooltip() {
			return switch(menu.blockEntity.mode) {
			case EXACT -> EXACT_TOOLTIP;
			case DISJOINT -> DISJOINT_TOOLTIP;
			case FIRST -> FIRST_TOOLTIP;
			};
		}
	}
}
