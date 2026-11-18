package thelm.packagedauto.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import thelm.packagedauto.block.entity.PackagingProviderBlockEntity;
import thelm.packagedauto.menu.PackagingProviderMenu;
import thelm.packagedauto.packet.ChangeBlockingPacket;
import thelm.packagedauto.packet.ChangeProvidingPacket;

public class PackagingProviderScreen extends BaseScreen<PackagingProviderMenu> {

	public static final Identifier BACKGROUND = Identifier.parse("packagedauto:textures/gui/packaging_provider.png");

	public PackagingProviderScreen(PackagingProviderMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	protected Identifier getBackgroundTexture() {
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

	class ButtonChangeBlocking extends AbstractButton {

		final Tooltip trueTooltip = Tooltip.create(Component.translatable("block.packagedauto.unpackager.blocking.true"));
		final Tooltip falseTooltip = Tooltip.create(Component.translatable("block.packagedauto.unpackager.blocking.false"));

		public ButtonChangeBlocking(int x, int y) {
			super(x, y, 16, 18, Component.empty());
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
			setTooltip(menu.blockEntity.blocking ? trueTooltip : falseTooltip);
			extractDefaultSprite(graphics);
			graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, getX()+1, getY()+2, 176, menu.blockEntity.blocking ? 14 : 0, 14, 14, 256, 256);
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

		@Override
		public void onPress(InputWithModifiers input) {
			ClientPacketDistributor.sendToServer(ChangeBlockingPacket.INSTANCE);
		}
	}

	class ButtonChangeProvideDirect extends AbstractButton {

		final Tooltip trueTooltip = Tooltip.create(Component.translatable("block.packagedauto.packaging_provider.direct.true"));
		final Tooltip falseTooltip = Tooltip.create(Component.translatable("block.packagedauto.packaging_provider.direct.false"));

		public ButtonChangeProvideDirect(int x, int y) {
			super(x, y, 16, 18, Component.empty());
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
			setTooltip(menu.blockEntity.provideDirect ? trueTooltip : falseTooltip);
			extractDefaultSprite(graphics);
			graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, getX()+1, getY()+2, 176, menu.blockEntity.provideDirect ? 42 : 28, 14, 14, 256, 256);
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

		@Override
		public void onPress(InputWithModifiers input) {
			ClientPacketDistributor.sendToServer(new ChangeProvidingPacket(PackagingProviderBlockEntity.Type.DIRECT));
		}
	}

	class ButtonChangeProvidePackaging extends AbstractButton {

		final Tooltip trueTooltip = Tooltip.create(Component.translatable("block.packagedauto.packaging_provider.packaging.true"));
		final Tooltip falseTooltip = Tooltip.create(Component.translatable("block.packagedauto.packaging_provider.packaging.false"));

		public ButtonChangeProvidePackaging(int x, int y) {
			super(x, y, 16, 18, Component.empty());
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
			setTooltip(menu.blockEntity.providePackaging ? trueTooltip : falseTooltip);
			extractDefaultSprite(graphics);
			graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, getX()+1, getY()+2, 176, menu.blockEntity.providePackaging ? 70 : 56, 14, 14, 256, 256);
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

		@Override
		public void onPress(InputWithModifiers input) {
			ClientPacketDistributor.sendToServer(new ChangeProvidingPacket(PackagingProviderBlockEntity.Type.PACKAGING));
		}
	}

	class ButtonChangeProvideUnpackaging extends AbstractButton {

		final Tooltip trueTooltip = Tooltip.create(Component.translatable("block.packagedauto.packaging_provider.unpackaging.true"));
		final Tooltip falseTooltip = Tooltip.create(Component.translatable("block.packagedauto.packaging_provider.unpackaging.false"));

		public ButtonChangeProvideUnpackaging(int x, int y) {
			super(x, y, 16, 18, Component.empty());
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
			setTooltip(menu.blockEntity.provideUnpackaging ? trueTooltip : falseTooltip);
			extractDefaultSprite(graphics);
			graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, getX()+1, getY()+2, 176, menu.blockEntity.provideUnpackaging ? 98 : 84, 14, 14, 256, 256);
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

		@Override
		public void onPress(InputWithModifiers input) {
			ClientPacketDistributor.sendToServer(new ChangeProvidingPacket(PackagingProviderBlockEntity.Type.UNPACKAGING));
		}
	}
}
