package thelm.packagedauto.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.item.ItemProxyMarker;

public class RecipeProxyMarkerCloning extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	public static final RecipeProxyMarkerCloning INSTANCE = new RecipeProxyMarkerCloning();

	protected RecipeProxyMarkerCloning() {
		setRegistryName("packagedauto:proxy_marker_cloning");
	}

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		DirectionalGlobalPos template = null;
		int count = 0;
		for(int i = 0; i < inv.getSizeInventory(); ++i) {
			ItemStack stack = inv.getStackInSlot(i);
			if(!stack.isEmpty()) {
				if(stack.getItem() == ItemProxyMarker.INSTANCE) {
					if(template == null) {
						DirectionalGlobalPos globalPos = ItemProxyMarker.INSTANCE.getDirectionalGlobalPos(stack);
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
				if(stack.getItem() == ItemProxyMarker.INSTANCE) {
					DirectionalGlobalPos globalPos = ItemProxyMarker.INSTANCE.getDirectionalGlobalPos(stack);
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
			ItemStack result = new ItemStack(ItemProxyMarker.INSTANCE, count);
			if(!clearing && count > 1) {
				ItemProxyMarker.INSTANCE.setDirectionalGlobalPos(result, template);
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
