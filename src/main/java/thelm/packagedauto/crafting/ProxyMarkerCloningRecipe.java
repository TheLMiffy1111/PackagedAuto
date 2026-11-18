package thelm.packagedauto.crafting;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.item.PackagedAutoItems;

public class ProxyMarkerCloningRecipe extends CustomRecipe {

	public static final ProxyMarkerCloningRecipe INSTANCE = new ProxyMarkerCloningRecipe();
	public static final MapCodec<ProxyMarkerCloningRecipe> CODEC = MapCodec.unit(INSTANCE);
	public static final StreamCodec<RegistryFriendlyByteBuf, ProxyMarkerCloningRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public RecipeSerializer<ProxyMarkerCloningRecipe> getSerializer() {
		return PackagedAutoRecipeSerializers.PROXY_MARKER_CLONING.get();
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		DirectionalGlobalPos template = null;
		int count = 0;
		for(int i = 0; i < input.size(); ++i) {
			ItemStack stack = input.getItem(i);
			if(!stack.isEmpty()) {
				if(stack.is(PackagedAutoItems.PROXY_MARKER)) {
					if(template == null) {
						if(stack.has(PackagedAutoDataComponents.MARKER_POS)) {
							template = stack.get(PackagedAutoDataComponents.MARKER_POS);
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
	public ItemStack assemble(CraftingInput input) {
		DirectionalGlobalPos template = null;
		boolean clearing = false;
		int count = 0;
		for(int i = 0; i < input.size(); ++i) {
			ItemStack stack = input.getItem(i);
			if(!stack.isEmpty()) {
				if(stack.is(PackagedAutoItems.PROXY_MARKER)) {
					if(stack.has(PackagedAutoDataComponents.MARKER_POS)) {
						if(template == null) {
							template = stack.get(PackagedAutoDataComponents.MARKER_POS);
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
			ItemStack result = PackagedAutoItems.PROXY_MARKER.toStack(count);
			if(!clearing && count > 1) {
				DataComponentPatch patch = DataComponentPatch.builder().
						set(PackagedAutoDataComponents.MARKER_POS.get(), template).
						build();
				result.applyComponents(patch);
			}
			return result;
		}
		else {
			return ItemStack.EMPTY;
		}
	}
}
