package thelm.packagedauto.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.item.DistributorMarkerItem;

public class DistributorMarkerCloningRecipe extends CustomRecipe {

	public static final RecipeSerializer<DistributorMarkerCloningRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(DistributorMarkerCloningRecipe::new);

	public DistributorMarkerCloningRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		DirectionalGlobalPos template = null;
		int count = 0;
		for(int i = 0; i < container.getContainerSize(); ++i) {
			ItemStack stack = container.getItem(i);
			if(!stack.isEmpty()) {
				if(stack.is(DistributorMarkerItem.INSTANCE)) {
					if(template == null) {
						DirectionalGlobalPos globalPos = DistributorMarkerItem.INSTANCE.getDirectionalGlobalPos(stack);
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
	public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
		DirectionalGlobalPos template = null;
		boolean clearing = false;
		int count = 0;
		for(int i = 0; i < container.getContainerSize(); ++i) {
			ItemStack stack = container.getItem(i);
			if(!stack.isEmpty()) {
				if(stack.is(DistributorMarkerItem.INSTANCE)) {
					DirectionalGlobalPos globalPos = DistributorMarkerItem.INSTANCE.getDirectionalGlobalPos(stack);
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
			ItemStack result = new ItemStack(DistributorMarkerItem.INSTANCE, count);
			if(!clearing && count > 1) {
				DistributorMarkerItem.INSTANCE.setDirectionalGlobalPos(result, template);
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
