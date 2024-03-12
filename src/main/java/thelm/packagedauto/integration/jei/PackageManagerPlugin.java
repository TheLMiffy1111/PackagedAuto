package thelm.packagedauto.integration.jei;

import java.util.List;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeListItem;
import thelm.packagedauto.integration.jei.category.PackageContentsCategory;
import thelm.packagedauto.integration.jei.category.PackageProcessingCategory;
import thelm.packagedauto.integration.jei.category.PackageRecipeCategory;
import thelm.packagedauto.integration.jei.category.PackagingCategory;

public class PackageManagerPlugin implements IRecipeManagerPlugin {

	@Override
	public <V> List<RecipeType<?>> getRecipeTypes(IFocus<V> focus) {
		if(focus.getTypedValue().getIngredient() instanceof ItemStack stack) {
			if(stack.getItem() instanceof IPackageItem) {
				switch(focus.getRole()) {
				case INPUT: return List.of(PackageRecipeCategory.TYPE, PackageProcessingCategory.TYPE, PackageContentsCategory.TYPE);
				case OUTPUT: return List.of(PackageRecipeCategory.TYPE, PackagingCategory.TYPE);
				default: break;
				}
			}
			if(stack.getItem() instanceof IPackageRecipeListItem) {
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
	public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
		if(focus.getTypedValue().getIngredient() instanceof ItemStack stack) {
			RecipeType<T> type = recipeCategory.getRecipeType();
			if(stack.getItem() instanceof IPackageItem packageItem) {
				IPackageRecipeInfo recipe = packageItem.getRecipeInfo(stack);
				int index = packageItem.getIndex(stack);
				if(recipe != null && recipe.validPatternIndex(index)) {
					if(PackageRecipeCategory.TYPE.equals(type) || PackageProcessingCategory.TYPE.equals(type)) {
						return (List<T>)List.of(recipe);
					}
					if(PackagingCategory.TYPE.equals(type) || PackageContentsCategory.TYPE.equals(type)) {
						return (List<T>)List.of(recipe.getPatterns().get(index));
					}
				}
			}
			if(stack.getItem() instanceof IPackageRecipeListItem recipeListItem) {
				if(PackageRecipeCategory.TYPE.equals(type) || PackageProcessingCategory.TYPE.equals(type)) {
					return (List<T>)recipeListItem.getRecipeList(Minecraft.getInstance().level, stack).getRecipeList();
				}
			}
		}
		return List.of();
	}

	@Override
	public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
		return List.of();
	}
}
