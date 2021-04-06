package thelm.packagedauto.integration.jei;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import thelm.packagedauto.api.IGuiIngredientWrapper;
import thelm.packagedauto.api.IRecipeLayoutWrapper;

public class RecipeLayoutWrapper implements IRecipeLayoutWrapper {

	private final IRecipeLayout recipeLayout;

	public RecipeLayoutWrapper(IRecipeLayout recipeLayout) {
		this.recipeLayout = recipeLayout;
	}

	@Override
	public ResourceLocation getCategoryUid() {
		return recipeLayout.getRecipeCategory().getUid();
	}

	@Override
	public Class<?> getCategoryRecipeClass() {
		return recipeLayout.getRecipeCategory().getRecipeClass();
	}

	@Override
	public Map<Integer, IGuiIngredientWrapper<ItemStack>> getItemStackIngredients() {
		return Maps.transformValues(recipeLayout.getItemStacks().getGuiIngredients(), GuiIngredientWrapper::new);
	}

	@Override
	public Map<Integer, IGuiIngredientWrapper<FluidStack>> getFluidStackIngredients() {
		return Maps.transformValues(recipeLayout.getFluidStacks().getGuiIngredients(), GuiIngredientWrapper::new);
	}

	@Override
	public <V> Map<Integer, IGuiIngredientWrapper<V>> getIngredients(Class<? extends V> ingredientClass) {
		return Maps.transformValues(recipeLayout.getIngredientsGroup(PackagedAutoJEIPlugin.jeiRuntime.getIngredientManager().getIngredientType(ingredientClass)).getGuiIngredients(), GuiIngredientWrapper::new);
	}
}
