package thelm.packagedauto.integration.jei;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.IRecipeListItem;
import thelm.packagedauto.integration.jei.category.PackageContentsCategory;
import thelm.packagedauto.integration.jei.category.PackageContentsWrapper;
import thelm.packagedauto.integration.jei.category.PackageProcessingCategory;
import thelm.packagedauto.integration.jei.category.PackageProcessingWrapper;
import thelm.packagedauto.integration.jei.category.PackageRecipeCategory;
import thelm.packagedauto.integration.jei.category.PackageRecipeWrapper;
import thelm.packagedauto.integration.jei.category.PackagingCategory;
import thelm.packagedauto.integration.jei.category.PackagingWrapper;
import thelm.packagedauto.recipe.RecipeTypeProcessing;

public class PackageRegistryPlugin implements IRecipeRegistryPlugin {

	private final IRecipeInfo exampleRecipe;

	public PackageRegistryPlugin() {
		exampleRecipe = RecipeTypeProcessing.INSTANCE.getNewRecipeInfo();
		exampleRecipe.generateFromStacks(Collections.singletonList(new ItemStack(Blocks.BARRIER)), Collections.singletonList(new ItemStack(Blocks.BARRIER)), null);
	}

	@Override
	public <V> List<String> getRecipeCategoryUids(IFocus<V> focus) {
		if(focus.getValue() instanceof ItemStack) {
			ItemStack stack = (ItemStack)focus.getValue();
			if(stack.getItem() instanceof IPackageItem) {
				switch(focus.getMode()) {
				case INPUT: return ImmutableList.of(PackageRecipeCategory.UID, PackageProcessingCategory.UID, PackageContentsCategory.UID);
				case OUTPUT: return ImmutableList.of(PackageRecipeCategory.UID, PackagingCategory.UID);
				}
			}
			if(stack.getItem() instanceof IRecipeListItem) {
				switch(focus.getMode()) {
				case INPUT: return ImmutableList.of(PackageRecipeCategory.UID, PackageProcessingCategory.UID);
				case OUTPUT: return ImmutableList.of(PackageRecipeCategory.UID);
				}
			}
		}
		return Collections.emptyList();
	}

	@Override
	public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
		if(focus.getValue() instanceof ItemStack) {
			String uid = recipeCategory.getUid();
			ItemStack stack = (ItemStack)focus.getValue();
			if(stack.getItem() instanceof IPackageItem) {
				IPackageItem packageItem = (IPackageItem)stack.getItem();
				IRecipeInfo recipe = packageItem.getRecipeInfo(stack);
				int index = packageItem.getIndex(stack);
				if(recipe != null && recipe.validPatternIndex(index)) {
					if(PackageRecipeCategory.UID.equals(uid)) {
						return (List<T>)Collections.singletonList(new PackageRecipeWrapper(recipe));
					}
					if(PackagingCategory.UID.equals(uid)) {
						return (List<T>)Collections.singletonList(new PackagingWrapper(recipe.getPatterns().get(index)));
					}
					if(PackageProcessingCategory.UID.equals(uid)) {
						return (List<T>)Collections.singletonList(new PackageProcessingWrapper(recipe));
					}
					if(PackageContentsCategory.UID.equals(uid)) {
						return (List<T>)Collections.singletonList(new PackageContentsWrapper(recipe.getPatterns().get(index)));
					}
				}
			}
			if(stack.getItem() instanceof IRecipeListItem) {
				List<IRecipeInfo> recipeList = ((IRecipeListItem)stack.getItem()).getRecipeList(stack).getRecipeList();
				if(PackageRecipeCategory.UID.equals(uid)) {
					return (List<T>)recipeList.stream().map(PackageRecipeWrapper::new).collect(Collectors.toList());
				}
				if(PackageProcessingCategory.UID.equals(uid)) {
					return (List<T>)recipeList.stream().map(PackageProcessingWrapper::new).collect(Collectors.toList());
				}
			}
		}
		return Collections.emptyList();
	}

	@Override
	public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory) {
		if(recipeCategory instanceof PackageRecipeCategory) {
			return (List<T>)Collections.singletonList(new PackageRecipeWrapper(exampleRecipe));
		}
		if(recipeCategory instanceof PackagingCategory) {
			return (List<T>)Collections.singletonList(new PackagingWrapper(exampleRecipe.getPatterns().get(0)));
		}
		if(recipeCategory instanceof PackageProcessingCategory) {
			return (List<T>)Collections.singletonList(new PackageProcessingWrapper(exampleRecipe));
		}
		if(recipeCategory instanceof PackageContentsCategory) {
			return (List<T>)Collections.singletonList(new PackageContentsWrapper(exampleRecipe.getPatterns().get(0)));
		}
		return Collections.emptyList();
	}
}
