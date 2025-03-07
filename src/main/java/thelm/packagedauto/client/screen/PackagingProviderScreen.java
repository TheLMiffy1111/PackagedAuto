package thelm.packagedauto.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thelm.packagedauto.block.entity.PackagingProviderBlockEntity;
import thelm.packagedauto.menu.PackagingProviderMenu;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.ChangeBlockingPacket;
import thelm.packagedauto.network.packet.ChangeProvidingPacket;

public class PackagingProviderScreen extends BaseScreen<PackagingProviderMenu> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/packaging_provider.png");

	public PackagingProviderScreen(PackagingProviderMenu menu, Inventory inventory, Component title) {
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
		addRenderableWidget(new ButtonChangeBlocking(leftPos+62, topPos+34));
		addRenderableWidget(new ButtonChangeProvideDirect(leftPos+98, topPos+34));
		addRenderableWidget(new ButtonChangeProvidePackaging(leftPos+116, topPos+34));
		addRenderableWidget(new ButtonChangeProvideUnpackaging(leftPos+134, topPos+34));
	}

	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
		String s = menu.blockEntity.getDisplayName().getString();
		font.draw(poseStack, s, imageWidth/2 - font.width(s)/2, 6, 0x404040);
		font.draw(poseStack, menu.inventory.getDisplayName().getString(), menu.getPlayerInvX(), menu.getPlayerInvY()-11, 0x404040);
		for(GuiEventListener child : children()) {
			if(child.isMouseOver(mouseX, mouseY) && child instanceof AbstractWidget button) {
				button.renderToolTip(poseStack, mouseX-leftPos, mouseY-topPos);
				break;
			}
		}
		super.renderLabels(poseStack, mouseX, mouseY);
	}

	class ButtonChangeBlocking extends AbstractWidget {

		public ButtonChangeBlocking(int x, int y) {
			super(x, y, 16, 18, TextComponent.EMPTY);
		}

		@Override
		protected void renderBg(PoseStack poseStack, Minecraft minecraft, int mouseX, int mouseY) {
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			RenderSystem.setShaderTexture(0, BACKGROUND);
			blit(poseStack, x+1, y+2, 176, menu.blockEntity.blocking ? 14 : 0, 14, 14);
		}

		@Override
		public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
			renderTooltip(poseStack, new TranslatableComponent("block.packagedauto.unpackager.blocking."+menu.blockEntity.blocking), mouseX, mouseY);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			PacketHandler.INSTANCE.sendToServer(ChangeBlockingPacket.INSTANCE);
		}

		@Override
		public void updateNarration(NarrationElementOutput narrationElementOutput) {}
	}

	class ButtonChangeProvideDirect extends AbstractWidget {

		public ButtonChangeProvideDirect(int x, int y) {
			super(x, y, 16, 18, TextComponent.EMPTY);
		}

		@Override
		protected void renderBg(PoseStack poseStack, Minecraft minecraft, int mouseX, int mouseY) {
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			RenderSystem.setShaderTexture(0, BACKGROUND);
			blit(poseStack, x+1, y+2, 176, menu.blockEntity.provideDirect ? 42 : 28, 14, 14);
		}

		@Override
		public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
			renderTooltip(poseStack, new TranslatableComponent("block.packagedauto.packaging_provider.direct."+menu.blockEntity.provideDirect), mouseX, mouseY);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			PacketHandler.INSTANCE.sendToServer(new ChangeProvidingPacket(PackagingProviderBlockEntity.Type.DIRECT));
		}

		@Override
		public void updateNarration(NarrationElementOutput narrationElementOutput) {}
	}

	class ButtonChangeProvidePackaging extends AbstractWidget {

		public ButtonChangeProvidePackaging(int x, int y) {
			super(x, y, 16, 18, TextComponent.EMPTY);
		}

		@Override
		protected void renderBg(PoseStack poseStack, Minecraft minecraft, int mouseX, int mouseY) {
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			RenderSystem.setShaderTexture(0, BACKGROUND);
			blit(poseStack, x+1, y+2, 176, menu.blockEntity.providePackaging ? 70 : 56, 14, 14);
		}

		@Override
		public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
			renderTooltip(poseStack, new TranslatableComponent("block.packagedauto.packaging_provider.packaging."+menu.blockEntity.providePackaging), mouseX, mouseY);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			PacketHandler.INSTANCE.sendToServer(new ChangeProvidingPacket(PackagingProviderBlockEntity.Type.PACKAGING));
		}

		@Override
		public void updateNarration(NarrationElementOutput narrationElementOutput) {}
	}

	class ButtonChangeProvideUnpackaging extends AbstractWidget {

		public ButtonChangeProvideUnpackaging(int x, int y) {
			super(x, y, 16, 18, TextComponent.EMPTY);
		}

		@Override
		protected void renderBg(PoseStack poseStack, Minecraft minecraft, int mouseX, int mouseY) {
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			RenderSystem.setShaderTexture(0, BACKGROUND);
			blit(poseStack, x+1, y+2, 176, menu.blockEntity.provideUnpackaging ? 98 : 84, 14, 14);
		}

		@Override
		public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
			renderTooltip(poseStack, new TranslatableComponent("block.packagedauto.packaging_provider.unpackaging."+menu.blockEntity.provideUnpackaging), mouseX, mouseY);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			PacketHandler.INSTANCE.sendToServer(new ChangeProvidingPacket(PackagingProviderBlockEntity.Type.UNPACKAGING));
		}

		@Override
		public void updateNarration(NarrationElementOutput narrationElementOutput) {}
	}
}
