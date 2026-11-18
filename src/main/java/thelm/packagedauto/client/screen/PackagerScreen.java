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
import thelm.packagedauto.block.entity.PackagerBlockEntity;
import thelm.packagedauto.menu.PackagerMenu;
import thelm.packagedauto.packet.ChangePackagingPacket;

public class PackagerScreen extends BaseScreen<PackagerMenu> {

	public static final Identifier BACKGROUND = Identifier.parse("packagedauto:textures/gui/packager.png");

	public PackagerScreen(PackagerMenu menu, Inventory inventory, Component title) {
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
		addRenderableWidget(new ButtonChangePackaging(leftPos+98, topPos+16));
	}

	@Override
	protected void extractBackgroundAdditional(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos+102, topPos+53, 176, 0, menu.blockEntity.getScaledProgress(22), 16, 256, 256);
		int scaledEnergy = menu.blockEntity.getScaledEnergy(40);
		graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos+10, topPos+10+40-scaledEnergy, 176, 16+40-scaledEnergy, 12, scaledEnergy, 256, 256);
	}

	@Override
	protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		super.extractTooltip(graphics, mouseX, mouseY);
		if(mouseX-leftPos >= 10 && mouseY-topPos >= 10 && mouseX-leftPos <= 21 && mouseY-topPos <= 49) {
			graphics.setTooltipForNextFrame(font, Component.literal(menu.blockEntity.getEnergyStorage().getAmountAsInt()+" / "+menu.blockEntity.getEnergyStorage().getCapacityAsInt()+" FE"), mouseX, mouseY);
		}
	}

	class ButtonChangePackaging extends AbstractButton {

		final Tooltip exactTooltip = Tooltip.create(PackagerBlockEntity.Mode.EXACT.getTooltip());
		final Tooltip disjointTooltip = Tooltip.create(PackagerBlockEntity.Mode.DISJOINT.getTooltip());
		final Tooltip firstTooltip = Tooltip.create(PackagerBlockEntity.Mode.FIRST.getTooltip());

		public ButtonChangePackaging(int x, int y) {
			super(x, y, 16, 18, Component.empty());
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
			setTooltip(currentTooltip());
			extractDefaultSprite(graphics);
			graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, getX()+1, getY()+2, 176, 56+14*menu.blockEntity.mode.ordinal(), 14, 14, 256, 256);
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

		@Override
		public void onPress(InputWithModifiers input) {
			ClientPacketDistributor.sendToServer(ChangePackagingPacket.INSTANCE);
		}

		private Tooltip currentTooltip() {
			return switch(menu.blockEntity.mode) {
			case EXACT -> exactTooltip;
			case DISJOINT -> disjointTooltip;
			case FIRST -> firstTooltip;
			};
		}
	}
}
