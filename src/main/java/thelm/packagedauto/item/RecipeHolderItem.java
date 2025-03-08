package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.component.PackagedAutoDataComponents;

public class RecipeHolderItem extends Item {

	protected RecipeHolderItem() {
		super(new Item.Properties());
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if(!level.isClientSide && player.isShiftKeyDown() && player.getItemInHand(hand).has(PackagedAutoDataComponents.RECIPE_LIST)) {
			ItemStack stack = player.getItemInHand(hand).copy();
			DataComponentPatch patch = DataComponentPatch.builder().
					remove(PackagedAutoDataComponents.RECIPE_LIST.get()).
					build();
			stack.applyComponents(patch);
			return InteractionResultHolder.success(stack);
		}
		return super.use(level, player, hand);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag isAdvanced) {
		if(stack.has(PackagedAutoDataComponents.RECIPE_LIST)) {
			List<IPackageRecipeInfo> recipeList = stack.get(PackagedAutoDataComponents.RECIPE_LIST);
			tooltip.add(Component.translatable("item.packagedauto.recipe_holder.recipes"));
			for(IPackageRecipeInfo recipe : recipeList) {
				MutableComponent component = recipe.getRecipeType().getDisplayName().append(": ");
				for(int i = 0; i < recipe.getOutputs().size(); ++i) {
					if(i != 0) {
						component.append(", ");
					}
					ItemStack is = recipe.getOutputs().get(i);
					if(is.has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK)) {
						IVolumeStackWrapper vs = is.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK);
						component.append(is.getCount()+"x").append(vs.getAmountDesc()).append(" ").
						append(ComponentUtils.wrapInSquareBrackets(vs.getDisplayName()));
					}
					else {
						component.append(is.getCount()+" ").append(is.getDisplayName());
					}
				}
				tooltip.add(component);
			}
		}
		super.appendHoverText(stack, context, tooltip, isAdvanced);
	}
}
