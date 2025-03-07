package thelm.packagedauto.crafting;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import thelm.packagedauto.api.IPackageRecipeList;
import thelm.packagedauto.item.RecipeHolderItem;

public class RecipeHolderCloningRecipe extends SpecialRecipe {

	public static final IRecipeSerializer<RecipeHolderCloningRecipe> SERIALIZER = (IRecipeSerializer<RecipeHolderCloningRecipe>)
			new SpecialRecipeSerializer<>(RecipeHolderCloningRecipe::new).setRegistryName("packagedauto:recipe_holder_cloning");

	public RecipeHolderCloningRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public boolean matches(CraftingInventory inv, World world) {
		IPackageRecipeList template = null;
		int copyCount = 0;
		for(int i = 0; i < inv.getContainerSize(); ++i) {
			ItemStack stack = inv.getItem(i);
			if(!stack.isEmpty()) {
				if(stack.getItem() == RecipeHolderItem.INSTANCE) {
					IPackageRecipeList recipeListObj = RecipeHolderItem.INSTANCE.getRecipeList(stack);
					if(!recipeListObj.getRecipeList().isEmpty()) {
						if(template != null) {
							return false;
						}
						template = recipeListObj;
					}
					else {
						++copyCount;
					}
				}
				else {
					return false;
				}
			}
		}
		return template != null && copyCount > 0;
	}

	@Override
	public ItemStack assemble(CraftingInventory inv) {
		IPackageRecipeList template = null;
		int copyCount = 0;
		for(int i = 0; i < inv.getContainerSize(); ++i) {
			ItemStack stack = inv.getItem(i);
			if(!stack.isEmpty()) {
				if(stack.getItem() == RecipeHolderItem.INSTANCE) {
					IPackageRecipeList recipeListObj = RecipeHolderItem.INSTANCE.getRecipeList(stack);
					if(!recipeListObj.getRecipeList().isEmpty()) {
						if(template != null) {
							return ItemStack.EMPTY;
						}
						template = recipeListObj;
					}
					else {
						++copyCount;
					}
				}
				else {
					return ItemStack.EMPTY;
				}
			}
		}
		if(template != null && copyCount > 0) {
			ItemStack result = new ItemStack(RecipeHolderItem.INSTANCE, copyCount+1);
			RecipeHolderItem.INSTANCE.setRecipeList(result, template);
			return result;
		}
		else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width*height >= 2;
	}
}
