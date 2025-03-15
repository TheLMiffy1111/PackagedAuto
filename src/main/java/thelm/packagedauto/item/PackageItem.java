package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.util.MiscHelper;

public class PackageItem extends Item {

	protected PackageItem() {
		super(new Item.Properties());
	}

	public static ItemStack makePackage(IPackageRecipeInfo recipeInfo, int index) {
		ItemStack stack = PackagedAutoItems.PACKAGE.toStack();
		if(recipeInfo != null) {
			DataComponentPatch patch = DataComponentPatch.builder().
					set(PackagedAutoDataComponents.PACKAGE_INDEX.get(), index).
					set(PackagedAutoDataComponents.RECIPE.get(), recipeInfo).
					build();
			stack.applyComponents(patch);
		}
		return stack;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if(!level.isClientSide && player.isShiftKeyDown()) {
			ItemStack stack = player.getItemInHand(hand).copy();
			ItemStack stack1 = stack.split(1);
			if(MiscHelper.INSTANCE.isPackage(stack1)) {
				IPackageRecipeInfo recipe = stack1.get(PackagedAutoDataComponents.RECIPE);
				int index = stack1.get(PackagedAutoDataComponents.PACKAGE_INDEX);
				if(recipe.validPatternIndex(index)) {
					IPackagePattern pattern = recipe.getPatterns().get(index);
					List<ItemStack> inputs = pattern.getInputs();
					for(int i = 0; i < inputs.size(); ++i) {
						ItemStack input = inputs.get(i).copy();
						if(!player.getInventory().add(input)) {
							ItemEntity item = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), input);
							item.setThrower(player);
							level.addFreshEntity(item);
						}
					}
				}
			}
			return InteractionResultHolder.success(stack);
		}
		return super.use(level, player, hand);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag isAdvanced) {
		if(MiscHelper.INSTANCE.isPackage(stack)) {
			IPackageRecipeInfo recipe = stack.get(PackagedAutoDataComponents.RECIPE);
			int index = stack.get(PackagedAutoDataComponents.PACKAGE_INDEX);
			if(recipe.validPatternIndex(index)) {
				tooltip.add(recipe.getRecipeType().getDisplayName().append(": "));
				for(ItemStack is : recipe.getOutputs()) {
					if(is.has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK)) {
						IVolumeStackWrapper vs = is.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK);
						tooltip.add(Component.literal(is.getCount()+"x").append(vs.getAmountDesc()).append(" ").
								append(ComponentUtils.wrapInSquareBrackets(vs.getDisplayName())));
					}
					else {
						tooltip.add(Component.literal(is.getCount()+" ").append(is.copy().getDisplayName()));
					}
				}
				tooltip.add(Component.translatable("item.packagedauto.package.index", index));
				tooltip.add(Component.translatable("item.packagedauto.package.items"));
				List<ItemStack> recipeInputs = recipe.getInputs();
				List<ItemStack> packageItems = recipeInputs.subList(9*index, Math.min(9*index+9, recipeInputs.size()));
				for(ItemStack is : packageItems) {
					if(is.has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK)) {
						IVolumeStackWrapper vs = is.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK);
						tooltip.add(Component.literal(is.getCount()+"x").append(vs.getAmountDesc()).append(" ").
								append(ComponentUtils.wrapInSquareBrackets(vs.getDisplayName())));
					}
					else {
						tooltip.add(Component.literal(is.getCount()+" ").append(is.copy().getDisplayName()));
					}
				}
			}
		}
		super.appendHoverText(stack, context, tooltip, isAdvanced);
	}
}
