package thelm.packagedauto.client.screen;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.fluids.FluidStack;
import thelm.packagedauto.client.FluidRenderer;
import thelm.packagedauto.menu.FluidPackageFillerMenu;
import thelm.packagedauto.packet.SetFluidAmountPacket;

public class FluidPackageFillerScreen extends BaseScreen<FluidPackageFillerMenu> {

	public static final Identifier BACKGROUND = Identifier.parse("packagedauto:textures/gui/fluid_package_filler.png");
	public static final FluidRenderer FLUID_RENDERER = new FluidRenderer(16, 52, 1);

	protected EditBox amountField;

	public FluidPackageFillerScreen(FluidPackageFillerMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	protected Identifier getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	protected void init() {
		clearWidgets();
		super.init();
		amountField = new EditBox(font, leftPos+30, topPos+57, 41, font.lineHeight, Component.empty());
		amountField.setBordered(false);
		amountField.setValue(String.valueOf(menu.blockEntity.requiredAmount));
		amountField.setTextColor(0xFFFFFFFF);
		amountField.setFilter(s->!menu.blockEntity.isWorking && s.matches("[0-9]*"));
		amountField.setResponder(s->{
			try {
				int amount = Integer.parseInt(s);
				if(amount < 0 && amount > 1000000000) {
					amountField.setValue(String.valueOf(Math.clamp(amount, 0, 1000000000)));
				}
				else if(amount != menu.blockEntity.requiredAmount) {
					menu.blockEntity.requiredAmount = amount;
					ClientPacketDistributor.sendToServer(new SetFluidAmountPacket(amount));
				}
			}
			catch(NumberFormatException e) {
				// NO OP
			}
		});
		addRenderableWidget(amountField);
	}

	@Override
	protected void extractBackgroundAdditional(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos+102, topPos+35, 176, 0, menu.blockEntity.getScaledProgress(22), 16, 256, 256);
		int scaledEnergy = menu.blockEntity.getScaledEnergy(40);
		graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos+10, topPos+10+40-scaledEnergy, 176, 16+40-scaledEnergy, 12, scaledEnergy, 256, 256);
		if(menu.blockEntity.isWorking) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos+102, topPos+30, 176, 61, 6, 5, 256, 256);
		}
		else {
			graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos+102, topPos+30, 176, 56, 6, 5, 256, 256);
		}

		if(!menu.blockEntity.currentFluid.isEmpty()) {
			FluidStack stack = menu.blockEntity.currentFluid.toStack(menu.blockEntity.amount);
			FLUID_RENDERER.render(graphics, leftPos+80, topPos+17, stack, menu.blockEntity.requiredAmount);
		}
	}

	@Override
	protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		super.extractTooltip(graphics, mouseX, mouseY);
		if(mouseX-leftPos >= 10 && mouseY-topPos >= 10 && mouseX-leftPos <= 21 && mouseY-topPos <= 49) {
			graphics.setTooltipForNextFrame(font, Component.literal(menu.blockEntity.getEnergyStorage().getAmountAsInt()+" / "+menu.blockEntity.getEnergyStorage().getCapacityAsInt()+" FE"), mouseX, mouseY);
		}
		if(!menu.blockEntity.isWorking && mouseX-leftPos >= 102 && mouseY-topPos >= 30 && mouseX-leftPos <= 107 && mouseY-topPos <= 34) {
			graphics.setTooltipForNextFrame(font, Component.translatable("block.packagedauto.fluid_package_filler.redstone"), mouseX, mouseY);
		}
		if(menu.blockEntity.isWorking && !menu.blockEntity.currentFluid.isEmpty() && mouseX-leftPos >= 80 && mouseY-topPos >= 17 && mouseX-leftPos <= 95 && mouseY-topPos <= 68) {
			graphics.setTooltipForNextFrame(font, Component.literal("").append(menu.blockEntity.currentFluid.getHoverName()).append(" "+menu.blockEntity.amount+" / "+menu.blockEntity.requiredAmount+" mB"), mouseX, mouseY);
		}
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if(amountField.keyPressed(event)) {
			return true;
		}
		InputConstants.Key mouseKey = InputConstants.getKey(event);
		if(minecraft.options.keyInventory.isActiveAndMatches(mouseKey) && amountField.isFocused()) {
			return true;
		}
		return super.keyPressed(event);
	}
}
