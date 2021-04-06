package thelm.packagedauto.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import thelm.packagedauto.container.UnpackagerContainer;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.ChangeBlockingPacket;
import thelm.packagedauto.tile.UnpackagerTile.PackageTracker;

public class UnpackagerScreen extends BaseScreen<UnpackagerContainer> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/unpackager.png");

	public UnpackagerScreen(UnpackagerContainer container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
	}

	@Override
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);
		int scaledEnergy = container.tile.getScaledEnergy(40);
		blit(matrixStack, guiLeft+10, guiTop+10+40-scaledEnergy, 176, 40-scaledEnergy, 12, scaledEnergy);
		for(int i = 0; i < container.tile.trackers.length; ++i) {
			PackageTracker tracker = container.tile.trackers[i];
			for(int j = 0; j < tracker.amount; ++j) {
				if(tracker.received.getBoolean(j)) {
					blit(matrixStack, guiLeft+115+6*j, guiTop+16+6*i, 176, 45, 6, 5);
				}
				else {
					blit(matrixStack, guiLeft+115+6*j, guiTop+16+6*i, 176, 40, 6, 5);
				}
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
		String s = container.tile.getDisplayName().getString();
		font.drawString(matrixStack, s, xSize/2 - font.getStringWidth(s)/2, 6, 0x404040);
		font.drawString(matrixStack, container.playerInventory.getDisplayName().getString(), container.getPlayerInvX(), container.getPlayerInvY()-11, 0x404040);
		if(mouseX-guiLeft >= 10 && mouseY-guiTop >= 10 && mouseX-guiLeft <= 21 && mouseY-guiTop <= 49) {
			renderTooltip(matrixStack, new StringTextComponent(container.tile.getEnergyStorage().getEnergyStored()+" / "+container.tile.getEnergyStorage().getMaxEnergyStored()+" FE"), mouseX-guiLeft, mouseY-guiTop);
		}
		for(Widget button : buttons) {
			if(button.isMouseOver(mouseX, mouseY)) {
				button.renderToolTip(matrixStack, mouseX-guiLeft, mouseY-guiTop);
				break;
			}
		}
	}

	@Override
	public void init() {
		buttons.clear();
		super.init();
		addButton(new ButtonChangeBlocking(guiLeft+98, guiTop+16));
	}

	class ButtonChangeBlocking extends Widget {

		public ButtonChangeBlocking(int x, int y) {
			super(x, y, 16, 18, StringTextComponent.EMPTY);
		}

		@Override
		public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
			super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
			RenderSystem.color4f(1F, 1F, 1F, 1F);
			minecraft.getTextureManager().bindTexture(BACKGROUND);
			blit(matrixStack, x+1, y+2, 176, container.tile.blocking ? 64 : 50, 14, 14);
		}

		@Override
		public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
			renderTooltip(matrixStack, new TranslationTextComponent("block.packagedauto.unpackager.blocking."+container.tile.blocking), mouseX, mouseY);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			PacketHandler.INSTANCE.sendToServer(new ChangeBlockingPacket());
			container.tile.changeBlockingMode();
		}
	}
}
