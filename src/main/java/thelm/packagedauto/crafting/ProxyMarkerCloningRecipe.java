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
		int count = 0;
		for(int i = 0; i < inv.getContainerSize(); ++i) {
			ItemStack stack = inv.getItem(i);
			if(!stack.isEmpty()) {
				if(stack.getItem() == ProxyMarkerItem.INSTANCE) {
					if(template == null) {
						DirectionalGlobalPos globalPos = ProxyMarkerItem.INSTANCE.getDirectionalGlobalPos(stack);
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
	public ItemStack assemble(CraftingInventory inv) {
		DirectionalGlobalPos template = null;
		boolean clearing = false;
		int count = 0;
		for(int i = 0; i < inv.getContainerSize(); ++i) {
			ItemStack stack = inv.getItem(i);
			if(!stack.isEmpty()) {
				if(stack.getItem() == ProxyMarkerItem.INSTANCE) {
					DirectionalGlobalPos globalPos = ProxyMarkerItem.INSTANCE.getDirectionalGlobalPos(stack);
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
			ItemStack result = new ItemStack(ProxyMarkerItem.INSTANCE, count);
			if(!clearing && count > 1) {
				ProxyMarkerItem.INSTANCE.setDirectionalGlobalPos(result, template);
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
