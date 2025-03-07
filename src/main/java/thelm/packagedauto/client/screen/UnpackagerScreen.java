package thelm.packagedauto.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import thelm.packagedauto.container.UnpackagerContainer;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.ChangeBlockingPacket;
import thelm.packagedauto.network.packet.TrackerCountPacket;
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
	public void init() {
		buttons.clear();
		super.init();
		addButton(new ButtonChangeBlocking(leftPos+98, topPos+16));
		addButton(new ButtonTrackerCount(true, leftPos+98, topPos+34));
		addButton(new ButtonTrackerCount(false, leftPos+106, topPos+34));
	}

	@Override
	protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		super.renderBg(matrixStack, partialTicks, mouseX, mouseY);
		int scaledEnergy = menu.tile.getScaledEnergy(40);
		blit(matrixStack, leftPos+10, topPos+10+40-scaledEnergy, 176, 40-scaledEnergy, 12, scaledEnergy);
		for(int i = 0; i < menu.tile.trackers.length; ++i) {
			PackageTracker tracker = menu.tile.trackers[i];
			for(int j = 0; j < 9; ++j) {
				if(j < tracker.amount) {
					if(tracker.received.getBoolean(j)) {
						blit(matrixStack, leftPos+115+6*j, topPos+16+6*i, 176, 45, 6, 5);
					}
					else {
						blit(matrixStack, leftPos+115+6*j, topPos+16+6*i, 176, 40, 6, 5);
					}
				}
				else if(i < menu.tile.trackerCount) {
					blit(matrixStack, leftPos+115+6*j, topPos+16+6*i, 182, 45, 6, 5);
				}
				else {
					blit(matrixStack, leftPos+115+6*j, topPos+16+6*i, 182, 40, 6, 5);
				}
			}
		}
	}

	@Override
	protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
		String s = menu.tile.getDisplayName().getString();
		font.draw(matrixStack, s, imageWidth/2 - font.width(s)/2, 6, 0x404040);
		font.draw(matrixStack, menu.playerInventory.getDisplayName().getString(), menu.getPlayerInvX(), menu.getPlayerInvY()-11, 0x404040);
		if(mouseX-leftPos >= 10 && mouseY-topPos >= 10 && mouseX-leftPos <= 21 && mouseY-topPos <= 49) {
			renderTooltip(matrixStack, new StringTextComponent(menu.tile.getEnergyStorage().getEnergyStored()+" / "+menu.tile.getEnergyStorage().getMaxEnergyStored()+" FE"), mouseX-leftPos, mouseY-topPos);
		}
		for(Widget button : buttons) {
			if(button.isMouseOver(mouseX, mouseY)) {
				button.renderToolTip(matrixStack, mouseX-leftPos, mouseY-topPos);
				break;
			}
		}
	}

	class ButtonChangeBlocking extends Widget {

		public ButtonChangeBlocking(int x, int y) {
			super(x, y, 16, 18, StringTextComponent.EMPTY);
		}

		@Override
		protected void renderBg(MatrixStack matrixStack, Minecraft minecraft, int mouseX, int mouseY) {
			RenderSystem.color4f(1F, 1F, 1F, 1F);
			minecraft.getTextureManager().bind(BACKGROUND);
			blit(matrixStack, x+1, y+2, 176, menu.tile.blocking ? 64 : 50, 14, 14);
		}

		@Override
		public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
			renderTooltip(matrixStack, new TranslationTextComponent("block.packagedauto.unpackager.blocking."+menu.tile.blocking), mouseX, mouseY);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			PacketHandler.INSTANCE.sendToServer(ChangeBlockingPacket.INSTANCE);
		}
	}

	class ButtonTrackerCount extends Widget {

		final boolean decrease;

		public ButtonTrackerCount(boolean decrease, int x, int y) {
			super(x, y, 8, 18, StringTextComponent.EMPTY);
			this.decrease = decrease;
		}

		@Override
		protected void renderBg(MatrixStack matrixStack, Minecraft minecraft, int mouseX, int mouseY) {
			RenderSystem.color4f(1F, 1F, 1F, 1F);
			minecraft.getTextureManager().bind(BACKGROUND);
			blit(matrixStack, x+1, y+2, decrease ? 176 : 182, 78, 6, 14);
		}

		@Override
		public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
			renderTooltip(matrixStack, new TranslationTextComponent("block.packagedauto.unpackager.tracker."+(decrease ? "decrease" : "increase")), mouseX, mouseY);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			PacketHandler.INSTANCE.sendToServer(new TrackerCountPacket(decrease));
		}
	}
}
