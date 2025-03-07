package thelm.packagedauto.crafting;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PackagedAutoRecipeSerializers {

	private PackagedAutoRecipeSerializers() {}

	public static <R extends CraftingRecipe> Supplier<RecipeSerializer<R>> of(SimpleCraftingRecipeSerializer.Factory<R> factory) {
		return ()->new SimpleCraftingRecipeSerializer<>(factory);
	}

	public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "packagedauto");

	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RecipeHolderCloningRecipe>> RECIPE_HOLDER_CLONING = RECIPE_SERIALIZERS.register("recipe_holder_cloning", of(RecipeHolderCloningRecipe::new));
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DistributorMarkerCloningRecipe>> DISTRIBUTOR_MARKER_CLONING = RECIPE_SERIALIZERS.register("distributor_marker_cloning", of(DistributorMarkerCloningRecipe::new));
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ProxyMarkerCloningRecipe>> proxy_marker_CLONING = RECIPE_SERIALIZERS.register("proxy_marker_cloning", of(ProxyMarkerCloningRecipe::new));
}
