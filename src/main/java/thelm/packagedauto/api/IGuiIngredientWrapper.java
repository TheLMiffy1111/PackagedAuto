package thelm.packagedauto.api;

import java.util.List;

public interface IGuiIngredientWrapper<T> {

	T getDisplayedIngredient();

	List<T> getAllIngredients();

	boolean isInput();
}
