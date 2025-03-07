package thelm.packagedauto.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.Level;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.item.ProxyMarkerItem;

public class ProxyMarkerCloningRecipe extends CustomRecipe {

	public static final RecipeSerializer<ProxyMarkerCloningRecipe> SERIALIZER = (RecipeSerializer<ProxyMarkerCloningRecipe>)
			new SimpleRecipeSerializer<>(ProxyMarkerCloningRecipe::new).setRegistryName("packagedauto:proxy_marker_cloning");

	public ProxyMarkerCloningRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		DirectionalGlobalPos template = null;
		int copyCount = 0;
		for(int i = 0; i < container.getContainerSize(); ++i) {
			ItemStack stack = container.getItem(i);
			if(!stack.isEmpty()) {
				if(stack.is(ProxyMarkerItem.INSTANCE)) {
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
	public ItemStack assemble(CraftingContainer container) {
		DirectionalGlobalPos template = null;
		int copyCount = 0;
		for(int i = 0; i < container.getContainerSize(); ++i) {
			ItemStack stack = container.getItem(i);
			if(!stack.isEmpty()) {
				if(stack.is(ProxyMarkerItem.INSTANCE)) {
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
