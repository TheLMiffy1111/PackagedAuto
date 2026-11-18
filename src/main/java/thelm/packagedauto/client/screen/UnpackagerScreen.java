package thelm.packagedauto.client.screen;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity.PackageTracker;
import thelm.packagedauto.menu.UnpackagerMenu;
import thelm.packagedauto.packet.ChangeBlockingPacket;
import thelm.packagedauto.packet.EjectTrackerPacket;
import thelm.packagedauto.packet.TrackerCountPacket;

public class UnpackagerScreen extends BaseScreen<UnpackagerMenu> {

	public static final Identifier BACKGROUND = Identifier.parse("packagedauto:textures/gui/unpackager.png");

	public UnpackagerScreen(UnpackagerMenu menu, Inventory inventory, Component title) {
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
		addRenderableWidget(new ButtonChangeBlocking(leftPos+98, topPos+16));
		for(int i = 0; i < 10; ++i) {
			addRenderableWidget(new ButtonTracker(i, leftPos+115, topPos+16+6*i));
		}
		addRenderableWidget(new ButtonTrackerCount(true, leftPos+98, topPos+34));
		addRenderableWidget(new ButtonTrackerCount(false, leftPos+106, topPos+34));
	}

	@Override
	protected void extractBackgroundAdditional(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		int scaledEnergy = menu.blockEntity.getScaledEnergy(40);
		graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos+10, topPos+10+40-scaledEnergy, 176, 40-scaledEnergy, 12, scaledEnergy, 256, 256);
		for(int i = 0; i < menu.blockEntity.trackers.length; ++i) {
			PackageTracker tracker = menu.blockEntity.trackers[i];
			for(int j = 0; j < 9; ++j) {
				if(j < tracker.amount) {
					if(tracker.received.getBoolean(j)) {
						graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos+115+6*j, topPos+16+6*i, 176, 45, 6, 5, 256, 256);
					}
					else {
						graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos+115+6*j, topPos+16+6*i, 176, 40, 6, 5, 256, 256);
					}
				}
				else if(i < menu.blockEntity.trackerCount) {
					graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos+115+6*j, topPos+16+6*i, 182, 45, 6, 5, 256, 256);
				}
				else {
					graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos+115+6*j, topPos+16+6*i, 182, 40, 6, 5, 256, 256);
				}
			}
		}
	}

	@Override
	protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		super.extractTooltip(graphics, mouseX, mouseY);
		if(mouseX-leftPos >= 10 && mouseY-topPos >= 10 && mouseX-leftPos <= 21 && mouseY-topPos <= 49) {
			graphics.setTooltipForNextFrame(font, Component.literal(menu.blockEntity.getEnergyStorage().getAmountAsInt()+" / "+menu.blockEntity.getEnergyStorage().getCapacityAsInt()+" FE"), mouseX, mouseY);
		}
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
			graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, getX()+1, getY()+2, 176, menu.blockEntity.blocking ? 64 : 50, 14, 14, 256, 256);
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

		@Override
		public void onPress(InputWithModifiers input) {
			ClientPacketDistributor.sendToServer(ChangeBlockingPacket.INSTANCE);
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
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

		@Override
		public void onPress(InputWithModifiers input) {
			if(input.hasShiftDown()) {
				ClientPacketDistributor.sendToServer(new EjectTrackerPacket(id));
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
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
			extractDefaultSprite(graphics);
			graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, getX()+1, getY()+2, decrease ? 176 : 182, 78, 6, 14, 256, 256);
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

		@Override
		public void onPress(InputWithModifiers input) {
			ClientPacketDistributor.sendToServer(new TrackerCountPacket(decrease));
		}
	}
}
