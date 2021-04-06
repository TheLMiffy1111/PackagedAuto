package thelm.packagedauto.api;

import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public interface IRecipeLayoutWrapper {

	ResourceLocation getCategoryUid();

	Class<?> getCategoryRecipeClass();

	Map<Integer, IGuiIngredientWrapper<ItemStack>> getItemStackIngredients();

	Map<Integer, IGuiIngredientWrapper<FluidStack>> getFluidStackIngredients();

	<V> Map<Integer, IGuiIngredientWrapper<V>> getIngredients(Class<? extends V> ingredientClass);
}
