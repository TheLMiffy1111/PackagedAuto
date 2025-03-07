package thelm.packagedauto.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.item.ItemDistributorMarker;

public class RecipeDistributorMarkerCloning extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	public static final RecipeDistributorMarkerCloning INSTANCE = new RecipeDistributorMarkerCloning();

	protected RecipeDistributorMarkerCloning() {
		setRegistryName("packagedauto:distributor_marker_cloning");
	}

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		DirectionalGlobalPos template = null;
		int copyCount = 0;
		for(int i = 0; i < inv.getSizeInventory(); ++i) {
			ItemStack stack = inv.getStackInSlot(i);
			if(!stack.isEmpty()) {
				if(stack.getItem() == ItemDistributorMarker.INSTANCE) {
					DirectionalGlobalPos globalPos = ItemDistributorMarker.INSTANCE.getDirectionalGlobalPos(stack);
					if(globalPos != null) {
						if(template != null) {
							return false;
						}
						template = globalPos;
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
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		DirectionalGlobalPos template = null;
		int copyCount = 0;
		for(int i = 0; i < inv.getSizeInventory(); ++i) {
			ItemStack stack = inv.getStackInSlot(i);
			if(!stack.isEmpty()) {
				if(stack.getItem() == ItemDistributorMarker.INSTANCE) {
					DirectionalGlobalPos globalPos = ItemDistributorMarker.INSTANCE.getDirectionalGlobalPos(stack);
					if(globalPos != null) {
						if(template != null) {
							return ItemStack.EMPTY;
						}
						template = globalPos;
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
			ItemStack result = new ItemStack(ItemDistributorMarker.INSTANCE, copyCount+1);
			ItemDistributorMarker.INSTANCE.setDirectionalGlobalPos(result, template);
			return result;
		}
		else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public boolean canFit(int width, int height) {
		return width*height >= 2;
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
