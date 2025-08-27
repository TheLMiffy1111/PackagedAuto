package thelm.packagedauto.integration.jei.category;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import thelm.packagedauto.api.IPackagePattern;

public class PackageContentsWrapper implements IRecipeWrapper {

	public final IPackagePattern pattern;

	public PackageContentsWrapper(IPackagePattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInput(VanillaTypes.ITEM, pattern.getOutput());
		ingredients.setOutputs(VanillaTypes.ITEM, pattern.getInputs());
	}
}
