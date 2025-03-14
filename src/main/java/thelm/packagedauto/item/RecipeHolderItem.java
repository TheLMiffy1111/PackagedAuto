package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
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
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeList;
import thelm.packagedauto.api.IPackageRecipeListItem;
import thelm.packagedauto.api.IVolumePackageItem;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.util.PackageRecipeList;

public class RecipeHolderItem extends Item implements IPackageRecipeListItem {

	public static final RecipeHolderItem INSTANCE = new RecipeHolderItem();

	protected RecipeHolderItem() {
		super(new Item.Properties().tab(PackagedAuto.CREATIVE_TAB));
	}

	@Override
	public IPackageRecipeList getRecipeList(ItemStack stack) {
		return new PackageRecipeList(stack.getTag());
	}

	@Override
	public void setRecipeList(ItemStack stack, IPackageRecipeList recipeList) {
		stack.getOrCreateTag().remove("Recipes");
		if(recipeList != null) {
			recipeList.save(stack.getTag());
		}
		if(stack.getTag().isEmpty()) {
			stack.setTag(null);
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if(!level.isClientSide && player.isShiftKeyDown() && isFilled(player.getItemInHand(hand))) {
			ItemStack stack = player.getItemInHand(hand).copy();
			setRecipeList(stack, null);
			return InteractionResultHolder.success(stack);
		}
		return super.use(level, player, hand);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
		if(stack.hasTag()) {
			List<IPackageRecipeInfo> recipeList = getRecipeList(stack).getRecipeList();
			tooltip.add(Component.translatable("item.packagedauto.recipe_holder.recipes"));
			for(IPackageRecipeInfo recipe : recipeList) {
				MutableComponent component = recipe.getRecipeType().getDisplayName().append(": ");
				for(int i = 0; i < recipe.getOutputs().size(); ++i) {
					if(i != 0) {
						component.append(", ");
					}
					ItemStack is = recipe.getOutputs().get(i);
					if(is.getItem() instanceof IVolumePackageItem vp) {
						IVolumeStackWrapper vs = vp.getVolumeStack(is);
						component.append(is.getCount()+"x").append(vs.getAmountDesc()).append(" ").
						append(ComponentUtils.wrapInSquareBrackets(vs.getDisplayName()));
					}
					else {
						component.append(is.getCount()+" ").append(is.copy().getDisplayName());
					}
				}
				tooltip.add(component);
			}
		}
		super.appendHoverText(stack, level, tooltip, isAdvanced);
	}

	public boolean isFilled(ItemStack stack) {
		CompoundTag nbt = stack.getTag();
		return nbt != null && !nbt.getList("Recipes", 10).isEmpty();
	}
}
