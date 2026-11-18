package thelm.packagedauto.integration.jei;

import java.util.List;
import java.util.Optional;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.integration.jei.category.FluidPackageContentsCategory;
import thelm.packagedauto.integration.jei.category.FluidPackageFillingCategory;
import thelm.packagedauto.volume.FluidVolumeType;

public class FluidPackageManagerPlugin implements IRecipeManagerPlugin {

	@Override
	public <V> List<IRecipeType<?>> getRecipeTypes(IFocus<V> focus) {
		V ingredient = focus.getTypedValue().getIngredient();
		if(ingredient instanceof ItemStack stack) {
			if(stack.has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK)) {
				IVolumeStackWrapper vStack = stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK);
				if(vStack.getVolumeType() == FluidVolumeType.INSTANCE) {
					switch(focus.getRole()) {
					case INPUT: return List.of(FluidPackageContentsCategory.TYPE);
					case OUTPUT: return List.of(FluidPackageFillingCategory.TYPE);
					default: break;
					}
				}
			}
		}
		if(ingredient instanceof FluidStack) {
			switch(focus.getRole()) {
			case INPUT: return List.of(FluidPackageFillingCategory.TYPE);
			case OUTPUT: return List.of(FluidPackageContentsCategory.TYPE);
			default: break;
			}
		}
		return List.of();
	}

	@Override
	public <T, V> List<T> getRecipes(IRecipeType<T> recipeType, IFocus<V> focus) {
		V ingredient = focus.getTypedValue().getIngredient();
		if(ingredient instanceof ItemStack stack) {
			if(stack.has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK)) {
				IVolumeStackWrapper vStack = stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK);
				if(vStack.getVolumeType() == FluidVolumeType.INSTANCE) {
					if(FluidPackageContentsCategory.TYPE.equals(recipeType) || FluidPackageFillingCategory.TYPE.equals(recipeType)) {
						return (List<T>)List.of(vStack);
					}
				}
			}
		}
		if(ingredient instanceof FluidStack stack) {
			if(FluidPackageContentsCategory.TYPE.equals(recipeType) || FluidPackageFillingCategory.TYPE.equals(recipeType)) {
				Optional<IVolumeStackWrapper> vStack = FluidVolumeType.INSTANCE.wrapStack(stack);
				if(vStack.isPresent()) {
					return (List<T>)List.of(vStack.get());
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
