package thelm.packagedauto.integration.jei;

import java.util.List;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import thelm.packagedauto.api.IVolumePackageItem;
import thelm.packagedauto.integration.jei.category.FluidPackageContentsCategory;
import thelm.packagedauto.integration.jei.category.FluidPackageFillingCategory;
import thelm.packagedauto.volume.FluidStackWrapper;
import thelm.packagedauto.volume.FluidVolumeType;

public class FluidPackageManagerPlugin implements IRecipeManagerPlugin {

	@Override
	public <V> List<RecipeType<?>> getRecipeTypes(IFocus<V> focus) {
		V ingredient = focus.getTypedValue().getIngredient();
		if(ingredient instanceof ItemStack stack) {
			if(stack.getItem() instanceof IVolumePackageItem vPackage) {
				if(vPackage.getVolumeType(stack) == FluidVolumeType.INSTANCE) {
					switch(focus.getRole()) {
					case INPUT: return List.of(FluidPackageContentsCategory.TYPE);
					case OUTPUT: return List.of(FluidPackageFillingCategory.TYPE);
					default: break;
					}
				}
			}
		}
		if(ingredient instanceof FluidStack stack) {
			switch(focus.getRole()) {
			case INPUT: return List.of(FluidPackageFillingCategory.TYPE);
			case OUTPUT: return List.of(FluidPackageContentsCategory.TYPE);
			default: break;
			}
		}
		return List.of();
	}

	@Override
	public <V> List<ResourceLocation> getRecipeCategoryUids(IFocus<V> focus) {
		return getRecipeTypes(focus).stream().map(RecipeType::getUid).toList();
	}

	@Override
	public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
		RecipeType<T> type = recipeCategory.getRecipeType();
		V ingredient = focus.getTypedValue().getIngredient();
		if(ingredient instanceof ItemStack stack) {
			if(stack.getItem() instanceof IVolumePackageItem vPackage) {
				if(vPackage.getVolumeType(stack) == FluidVolumeType.INSTANCE) {
					if(FluidPackageContentsCategory.TYPE.equals(type) || FluidPackageFillingCategory.TYPE.equals(type)) {
						return (List<T>)List.of(vPackage.getVolumeStack(stack));
					}
				}
			}
		}
		if(ingredient instanceof FluidStack stack) {
			if(FluidPackageContentsCategory.TYPE.equals(type) || FluidPackageFillingCategory.TYPE.equals(type)) {
				return (List<T>)List.of(new FluidStackWrapper(stack));
			}
		}
		return List.of();
	}

	@Override
	public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
		return List.of();
	}
}
