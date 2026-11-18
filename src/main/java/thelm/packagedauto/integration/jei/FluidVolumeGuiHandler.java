package thelm.packagedauto.integration.jei;

import java.util.Optional;

import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import thelm.packagedauto.api.IFluidStackWrapper;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.slot.FalseCopyVolumeSlot;

public class FluidVolumeGuiHandler implements IGuiContainerHandler<AbstractContainerScreen<?>> {

	private final IIngredientManager ingredientManager;

	public FluidVolumeGuiHandler(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}

	@Override
	public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(IClickableIngredientFactory builder, AbstractContainerScreen<?> containerScreen, double mouseX, double mouseY) {
		if(containerScreen.getHoveredSlot() instanceof FalseCopyVolumeSlot volumeSlot) {
			IVolumeStackWrapper volumeStack = volumeSlot.volumeInventory.getStack(volumeSlot.index);
			if(volumeStack instanceof IFluidStackWrapper fluidVolumeStack) {
				return ingredientManager.createTypedIngredient(fluidVolumeStack.getFluid(), true).
						flatMap(ing->builder.createBuilder(ing).buildWithArea(getSlotArea(containerScreen, volumeSlot)));
			}
		}
		return Optional.empty();
	}

	private static Rect2i getSlotArea(AbstractContainerScreen<?> gui, Slot slot) {
		return new Rect2i(gui.getLeftPos()+slot.x, gui.getTopPos()+slot.y, 16, 16);
	}
}
