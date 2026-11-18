package thelm.packagedauto.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import thelm.packagedauto.menu.CrafterMenu;

public class CrafterScreen extends BaseScreen<CrafterMenu> {

	public static final Identifier BACKGROUND = Identifier.parse("packagedauto:textures/gui/crafter.png");

	public CrafterScreen(CrafterMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	protected Identifier getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	protected void extractBackgroundAdditional(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos+102, topPos+35, 176, 0, menu.blockEntity.getScaledProgress(22), 16, 256, 256);
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
}
