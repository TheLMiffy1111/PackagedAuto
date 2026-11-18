package thelm.packagedauto.integration.jei;

import java.util.List;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.integration.jei.category.PackageContentsCategory;
import thelm.packagedauto.integration.jei.category.PackageProcessingCategory;
import thelm.packagedauto.integration.jei.category.PackageRecipeCategory;
import thelm.packagedauto.integration.jei.category.PackagingCategory;
import thelm.packagedauto.util.MiscHelper;

public class PackageManagerPlugin implements IRecipeManagerPlugin {

	@Override
	public <V> List<IRecipeType<?>> getRecipeTypes(IFocus<V> focus) {
		if(focus.getTypedValue().getIngredient() instanceof ItemStack stack) {
			if(MiscHelper.INSTANCE.isPackage(stack)) {
				switch(focus.getRole()) {
				case INPUT: return List.of(PackageRecipeCategory.TYPE, PackageProcessingCategory.TYPE, PackageContentsCategory.TYPE);
				case OUTPUT: return List.of(PackageRecipeCategory.TYPE, PackagingCategory.TYPE);
				default: break;
				}
			}
			if(stack.has(PackagedAutoDataComponents.RECIPE_LIST)) {
				switch(focus.getRole()) {
				case INPUT: return List.of(PackageRecipeCategory.TYPE, PackageProcessingCategory.TYPE);
				case OUTPUT: return List.of(PackageRecipeCategory.TYPE);
				default: break;
				}
			}
		}
		return List.of();
	}

	@Override
	public <T, V> List<T> getRecipes(IRecipeType<T> recipeType, IFocus<V> focus) {
		if(focus.getTypedValue().getIngredient() instanceof ItemStack stack) {
			if(MiscHelper.INSTANCE.isPackage(stack)) {
				IPackageRecipeInfo recipe = stack.get(PackagedAutoDataComponents.RECIPE);
				int index = stack.get(PackagedAutoDataComponents.PACKAGE_INDEX);
				if(recipe.validPatternIndex(index)) {
					if(PackageRecipeCategory.TYPE.equals(recipeType) || PackageProcessingCategory.TYPE.equals(recipeType)) {
						return (List<T>)List.of(recipe);
					}
					if(PackagingCategory.TYPE.equals(recipeType) || PackageContentsCategory.TYPE.equals(recipeType)) {
						return (List<T>)List.of(recipe.getPatterns().get(index));
					}
				}
			}
			if(stack.has(PackagedAutoDataComponents.RECIPE_LIST)) {
				if(PackageRecipeCategory.TYPE.equals(recipeType) || PackageProcessingCategory.TYPE.equals(recipeType)) {
					return (List<T>)stack.get(PackagedAutoDataComponents.RECIPE_LIST);
				}
			}
		}
		return List.of();
	}

	@Override
	public <T> List<T> getRecipes(IRecipeType<T> recipeType) {
		return List.of();
	}
}
