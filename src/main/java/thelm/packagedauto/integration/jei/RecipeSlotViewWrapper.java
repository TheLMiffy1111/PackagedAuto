package thelm.packagedauto.integration.jei;

import java.util.List;
import java.util.Optional;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import thelm.packagedauto.api.IRecipeSlotViewWrapper;

public class RecipeSlotViewWrapper implements IRecipeSlotViewWrapper {

	private final IRecipeSlotView recipeSlotView;

	public RecipeSlotViewWrapper(IRecipeSlotView recipeSlotView) {
		this.recipeSlotView = recipeSlotView;
	}

	@Override
	public Optional<?> getDisplayedIngredient() {
		return recipeSlotView.getDisplayedIngredient().map(i->i.getIngredient());
	}

	@Override
	public List<?> getAllIngredients() {
		return recipeSlotView.getAllIngredients().map(i->i.getIngredient()).toList();
	}

	@Override
	public boolean isInput() {
		return recipeSlotView.getRole() == RecipeIngredientRole.INPUT;
	}

	@Override
	public boolean isOutput() {
		return recipeSlotView.getRole() == RecipeIngredientRole.OUTPUT;
	}
}
