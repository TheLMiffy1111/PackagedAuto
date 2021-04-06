package thelm.packagedauto.integration.jei;

import java.util.List;

import mezz.jei.api.gui.ingredient.IGuiIngredient;
import thelm.packagedauto.api.IGuiIngredientWrapper;

public class GuiIngredientWrapper<T> implements IGuiIngredientWrapper<T> {

	private final IGuiIngredient<T> guiIngredient;

	public GuiIngredientWrapper(IGuiIngredient<T> guiIngredient) {
		this.guiIngredient = guiIngredient;
	}

	@Override
	public T getDisplayedIngredient() {
		return guiIngredient.getDisplayedIngredient();
	}

	@Override
	public List<T> getAllIngredients() {
		return guiIngredient.getAllIngredients();
	}

	@Override
	public boolean isInput() {
		return guiIngredient.isInput();
	}
}
