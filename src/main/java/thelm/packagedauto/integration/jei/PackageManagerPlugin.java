package thelm.packagedauto.integration.jei;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeListItem;
import thelm.packagedauto.integration.jei.category.PackageContentsCategory;
import thelm.packagedauto.integration.jei.category.PackageProcessingCategory;
import thelm.packagedauto.integration.jei.category.PackageRecipeCategory;
import thelm.packagedauto.integration.jei.category.PackagingCategory;

public class PackageManagerPlugin implements IRecipeManagerPlugin {

	@Override
	public <V> List<ResourceLocation> getRecipeCategoryUids(IFocus<V> focus) {
		if(focus.getValue() instanceof ItemStack) {
			ItemStack stack = (ItemStack)focus.getValue();
			if(stack.getItem() instanceof IPackageItem) {
				switch(focus.getMode()) {
				case INPUT: return ImmutableList.of(PackageRecipeCategory.UID, PackageProcessingCategory.UID, PackageContentsCategory.UID);
				case OUTPUT: return ImmutableList.of(PackageRecipeCategory.UID, PackagingCategory.UID);
				}
			}
			if(stack.getItem() instanceof IPackageRecipeListItem) {
				switch(focus.getMode()) {
				case INPUT: return ImmutableList.of(PackageRecipeCategory.UID, PackageProcessingCategory.UID);
				case OUTPUT: return ImmutableList.of(PackageRecipeCategory.UID);
				}
			}
		}
		return Collections.emptyList();
	}

	@Override
	public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
		if(focus.getValue() instanceof ItemStack) {
			ResourceLocation uid = recipeCategory.getUid();
			ItemStack stack = (ItemStack)focus.getValue();
			if(stack.getItem() instanceof IPackageItem) {
				IPackageItem packageItem = (IPackageItem)stack.getItem();
				IPackageRecipeInfo recipe = packageItem.getRecipeInfo(stack);
				int index = packageItem.getIndex(stack);
				if(recipe != null && recipe.validPatternIndex(index)) {
					if(PackageRecipeCategory.UID.equals(uid) || PackageProcessingCategory.UID.equals(uid)) {
						return (List<T>)Collections.singletonList(recipe);
					}
					if(PackagingCategory.UID.equals(uid) || PackageContentsCategory.UID.equals(uid)) {
						return (List<T>)Collections.singletonList(recipe.getPatterns().get(index));
					}
				}
			}
			if(stack.getItem() instanceof IPackageRecipeListItem) {
				List<IPackageRecipeInfo> recipeList = ((IPackageRecipeListItem)stack.getItem()).getRecipeList(Minecraft.getInstance().level, stack).getRecipeList();
				if(PackageRecipeCategory.UID.equals(uid) || PackageProcessingCategory.UID.equals(uid)) {
					return (List<T>)recipeList;
				}
			}
		}
		return Collections.emptyList();
	}

	@Override
	public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
		return Collections.emptyList();
	}
}
