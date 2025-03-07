package thelm.packagedauto.crafting;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.item.ProxyMarkerItem;

public class ProxyMarkerCloningRecipe extends SpecialRecipe {

	public static final IRecipeSerializer<ProxyMarkerCloningRecipe> SERIALIZER = (IRecipeSerializer<ProxyMarkerCloningRecipe>)
			new SpecialRecipeSerializer<>(ProxyMarkerCloningRecipe::new).setRegistryName("packagedauto:proxy_marker_cloning");

	public ProxyMarkerCloningRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public boolean matches(CraftingInventory inv, World world) {
		DirectionalGlobalPos template = null;
		int copyCount = 0;
		for(int i = 0; i < inv.getContainerSize(); ++i) {
			ItemStack stack = inv.getItem(i);
			if(!stack.isEmpty()) {
				if(stack.getItem() == ProxyMarkerItem.INSTANCE) {
					DirectionalGlobalPos globalPos = ProxyMarkerItem.INSTANCE.getDirectionalGlobalPos(stack);
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
	public ItemStack assemble(CraftingInventory inv) {
		DirectionalGlobalPos template = null;
		int copyCount = 0;
		for(int i = 0; i < inv.getContainerSize(); ++i) {
			ItemStack stack = inv.getItem(i);
			if(!stack.isEmpty()) {
				if(stack.getItem() == ProxyMarkerItem.INSTANCE) {
					DirectionalGlobalPos globalPos = ProxyMarkerItem.INSTANCE.getDirectionalGlobalPos(stack);
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
			ItemStack result = new ItemStack(ProxyMarkerItem.INSTANCE, copyCount+1);
			ProxyMarkerItem.INSTANCE.setDirectionalGlobalPos(result, template);
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
