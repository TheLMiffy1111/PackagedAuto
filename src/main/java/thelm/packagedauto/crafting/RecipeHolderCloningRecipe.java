package thelm.packagedauto.crafting;

import java.util.List;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.item.PackagedAutoItems;

public class RecipeHolderCloningRecipe extends CustomRecipe {

	public static final RecipeHolderCloningRecipe INSTANCE = new RecipeHolderCloningRecipe();
	public static final MapCodec<RecipeHolderCloningRecipe> CODEC = MapCodec.unit(INSTANCE);
	public static final StreamCodec<RegistryFriendlyByteBuf, RecipeHolderCloningRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public RecipeSerializer<RecipeHolderCloningRecipe> getSerializer() {
		return PackagedAutoRecipeSerializers.RECIPE_HOLDER_CLONING.get();
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		List<IPackageRecipeInfo> template = null;
		int count = 0;
		for(int i = 0; i < input.size(); ++i) {
			ItemStack stack = input.getItem(i);
			if(!stack.isEmpty()) {
				if(stack.is(PackagedAutoItems.RECIPE_HOLDER)) {
					if(template == null) {
						if(stack.has(PackagedAutoDataComponents.RECIPE_LIST)) {
							template = stack.get(PackagedAutoDataComponents.RECIPE_LIST);
						}
					}
					++count;
				}
				else {
					return false;
				}
			}
		}
		return template != null && count > 0;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		List<IPackageRecipeInfo> template = null;
		boolean clearing = false;
		int count = 0;
		for(int i = 0; i < input.size(); ++i) {
			ItemStack stack = input.getItem(i);
			if(!stack.isEmpty()) {
				if(stack.is(PackagedAutoItems.RECIPE_HOLDER)) {
					if(stack.has(PackagedAutoDataComponents.RECIPE_LIST)) {
						if(template == null) {
							template = stack.get(PackagedAutoDataComponents.RECIPE_LIST);
						}
						else {
							clearing = true;
						}
					}
					++count;
				}
				else {
					return ItemStack.EMPTY;
				}
			}
		}
		if(template != null && count > 0) {
			ItemStack result = PackagedAutoItems.RECIPE_HOLDER.toStack(count);
			if(!clearing && count > 1) {
				DataComponentPatch patch = DataComponentPatch.builder().
						set(PackagedAutoDataComponents.RECIPE_LIST.get(), template).
						build();
				result.applyComponents(patch);
			}
			return result;
		}
		else {
			return ItemStack.EMPTY;
		}
	}
}
