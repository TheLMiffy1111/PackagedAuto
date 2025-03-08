package thelm.packagedauto.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;
import thelm.packagedauto.api.IRecipeList;
import thelm.packagedauto.item.ItemRecipeHolder;

public class RecipeRecipeHolderCloning extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	public static final RecipeRecipeHolderCloning INSTANCE = new RecipeRecipeHolderCloning();

	protected RecipeRecipeHolderCloning() {
		setRegistryName("packagedauto:recipe_holder_cloning");
	}

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		IRecipeList template = null;
		int count = 0;
		for(int i = 0; i < inv.getSizeInventory(); ++i) {
			ItemStack stack = inv.getStackInSlot(i);
			if(!stack.isEmpty()) {
				if(stack.getItem() == ItemRecipeHolder.INSTANCE) {
					if(template == null) {
						IRecipeList recipeListObj = ItemRecipeHolder.INSTANCE.getRecipeList(stack);
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
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		IRecipeList template = null;
		boolean clearing = false;
		int count = 0;
		for(int i = 0; i < inv.getSizeInventory(); ++i) {
			ItemStack stack = inv.getStackInSlot(i);
			if(!stack.isEmpty()) {
				if(stack.getItem() == ItemRecipeHolder.INSTANCE) {
					IRecipeList recipeListObj = ItemRecipeHolder.INSTANCE.getRecipeList(stack);
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
			ItemStack result = new ItemStack(ItemRecipeHolder.INSTANCE, count);
			if(!clearing && count > 1) {
				ItemRecipeHolder.INSTANCE.setRecipeList(result, template);
			}
			return result;
		}
		else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public boolean canFit(int width, int height) {
		return true;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}
}
