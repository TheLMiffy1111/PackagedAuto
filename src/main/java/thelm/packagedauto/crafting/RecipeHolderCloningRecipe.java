package thelm.packagedauto.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.Level;
import thelm.packagedauto.api.IPackageRecipeList;
import thelm.packagedauto.item.RecipeHolderItem;

public class RecipeHolderCloningRecipe extends CustomRecipe {

	public static final RecipeSerializer<RecipeHolderCloningRecipe> SERIALIZER = (RecipeSerializer<RecipeHolderCloningRecipe>)
			new SimpleRecipeSerializer<>(RecipeHolderCloningRecipe::new).setRegistryName("packagedauto:recipe_holder_cloning");

	public RecipeHolderCloningRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		IPackageRecipeList template = null;
		int count = 0;
		for(int i = 0; i < container.getContainerSize(); ++i) {
			ItemStack stack = container.getItem(i);
			if(!stack.isEmpty()) {
				if(stack.is(RecipeHolderItem.INSTANCE)) {
					if(template == null) {
						IPackageRecipeList recipeListObj = RecipeHolderItem.INSTANCE.getRecipeList(stack);
						if(!recipeListObj.getRecipeList().isEmpty()) {
							template = recipeListObj;
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
	public ItemStack assemble(CraftingContainer container) {
		IPackageRecipeList template = null;
		boolean clearing = false;
		int count = 0;
		for(int i = 0; i < container.getContainerSize(); ++i) {
			ItemStack stack = container.getItem(i);
			if(!stack.isEmpty()) {
				if(stack.is(RecipeHolderItem.INSTANCE)) {
					IPackageRecipeList recipeListObj = RecipeHolderItem.INSTANCE.getRecipeList(stack);
					if(!recipeListObj.getRecipeList().isEmpty()) {
						if(template == null) {
							template = recipeListObj;
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
			ItemStack result = new ItemStack(RecipeHolderItem.INSTANCE, count);
			if(!clearing && count > 1) {
				RecipeHolderItem.INSTANCE.setRecipeList(result, template);
			}
			return result;
		}
		else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return true;
	}
}
