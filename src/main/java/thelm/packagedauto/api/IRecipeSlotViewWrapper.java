package thelm.packagedauto.api;

import java.util.List;
import java.util.Optional;

public interface IRecipeSlotViewWrapper {

	Optional<?> getDisplayedIngredient();

	List<?> getAllIngredients();

	boolean isInput();

	boolean isOutput();
}
