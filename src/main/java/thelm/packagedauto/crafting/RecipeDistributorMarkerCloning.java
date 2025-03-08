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
		int count = 0;
		for(int i = 0; i < inv.getSizeInventory(); ++i) {
			ItemStack stack = inv.getStackInSlot(i);
			if(!stack.isEmpty()) {
				if(stack.getItem() == ItemDistributorMarker.INSTANCE) {
					if(template == null) {
						DirectionalGlobalPos globalPos = ItemDistributorMarker.INSTANCE.getDirectionalGlobalPos(stack);
						if(globalPos != null) {
							template = globalPos;
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
		DirectionalGlobalPos template = null;
		boolean clearing = false;
		int count = 0;
		for(int i = 0; i < inv.getSizeInventory(); ++i) {
			ItemStack stack = inv.getStackInSlot(i);
			if(!stack.isEmpty()) {
				if(stack.getItem() == ItemDistributorMarker.INSTANCE) {
					DirectionalGlobalPos globalPos = ItemDistributorMarker.INSTANCE.getDirectionalGlobalPos(stack);
					if(globalPos != null) {
						if(template == null) {
							template = globalPos;
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
			ItemStack result = new ItemStack(ItemDistributorMarker.INSTANCE, count);
			if(!clearing && count > 1) {
				ItemDistributorMarker.INSTANCE.setDirectionalGlobalPos(result, template);
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
