package thelm.packagedauto.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import thelm.packagedauto.container.PackagerContainer;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.ChangePackagingPacket;

public class PackagerScreen extends BaseScreen<PackagerContainer> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/packager.png");

	public PackagerScreen(PackagerContainer container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
	}

	@Override
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	public void init() {
		buttons.clear();
		super.init();
		addButton(new ButtonChangePackaging(leftPos+98, topPos+16));
	}

	@Override
	protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		super.renderBg(matrixStack, partialTicks, mouseX, mouseY);
		blit(matrixStack, leftPos+102, topPos+53, 176, 0, container.tile.getScaledProgress(22), 16);
		int scaledEnergy = container.tile.getScaledEnergy(40);
		blit(matrixStack, leftPos+10, topPos+10+40-scaledEnergy, 176, 16+40-scaledEnergy, 12, scaledEnergy);
	}

	@Override
	protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
		String s = container.tile.getDisplayName().getString();
		font.draw(matrixStack, s, imageWidth/2 - font.width(s)/2, 6, 0x404040);
		font.draw(matrixStack, container.playerInventory.getDisplayName().getString(), container.getPlayerInvX(), container.getPlayerInvY()-11, 0x404040);
		if(mouseX-leftPos >= 10 && mouseY-topPos >= 10 && mouseX-leftPos <= 21 && mouseY-topPos <= 49) {
			renderTooltip(matrixStack, new StringTextComponent(container.tile.getEnergyStorage().getEnergyStored()+" / "+container.tile.getEnergyStorage().getMaxEnergyStored()+" FE"), mouseX-leftPos, mouseY-topPos);
		}
		for(Widget button : buttons) {
			if(button.isMouseOver(mouseX, mouseY)) {
				button.renderToolTip(matrixStack, mouseX-leftPos, mouseY-topPos);
				break;
			}
		}
	}

	class ButtonChangePackaging extends Widget {

		ButtonChangePackaging(int x, int y) {
			super(x, y, 16, 18, StringTextComponent.EMPTY);
		}

		@Override
		public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
			super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
			RenderSystem.color4f(1F, 1F, 1F, 1F);
			minecraft.getTextureManager().bind(BACKGROUND);
			blit(matrixStack, x+1, y+2, 176, 56+14*container.tile.mode.ordinal(), 14, 14);
		}

		@Override
		public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
			renderTooltip(matrixStack, container.tile.mode.getTooltip(), mouseX, mouseY);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			PacketHandler.INSTANCE.sendToServer(new ChangePackagingPacket());
			container.tile.changePackagingMode();
		}
	}
}
