package thelm.packagedauto.integration.jei;

import java.util.Optional;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
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

	@SuppressWarnings("removal")
	@Override
	public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(AbstractContainerScreen<?> containerScreen, double mouseX, double mouseY) {
		if(containerScreen.getSlotUnderMouse() instanceof FalseCopyVolumeSlot volumeSlot) {
			IVolumeStackWrapper volumeStack = volumeSlot.volumeInventory.getStackInSlot(volumeSlot.slotIndex);
			if(volumeStack instanceof IFluidStackWrapper fluidVolumeStack) {
				return ingredientManager.createTypedIngredient(fluidVolumeStack.getFluid()).
						map(ing->new ClickableIngredient<>(ing, getSlotArea(containerScreen, volumeSlot)));
			}
		}
		return Optional.empty();
	}

	private static Rect2i getSlotArea(AbstractContainerScreen<?> gui, Slot slot) {
		return new Rect2i(gui.getGuiLeft()+slot.x, gui.getGuiTop()+slot.y, 16, 16);
	}

	private static record ClickableIngredient<T>(ITypedIngredient<T> ingredient, Rect2i area) implements IClickableIngredient<T> {

		@Override
		public ITypedIngredient<T> getTypedIngredient() {
			return ingredient;
		}

		@Override
		public Rect2i getArea() {
			return area;
		}
	}
}
