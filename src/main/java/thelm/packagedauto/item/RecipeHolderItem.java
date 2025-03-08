package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeList;
import thelm.packagedauto.api.IPackageRecipeListItem;
import thelm.packagedauto.util.PackageRecipeList;

public class RecipeHolderItem extends Item implements IPackageRecipeListItem {

	public static final RecipeHolderItem INSTANCE = new RecipeHolderItem();

	protected RecipeHolderItem() {
		super(new Item.Properties().tab(PackagedAuto.ITEM_GROUP));
		setRegistryName("packagedauto:recipe_holder");
	}

	@Override
	public IPackageRecipeList getRecipeList(ItemStack stack) {
		return new PackageRecipeList(stack.getTag());
	}

	@Override
	public void setRecipeList(ItemStack stack, IPackageRecipeList recipeList) {
		stack.getOrCreateTag().remove("Recipes");
		if(recipeList != null) {
			recipeList.write(stack.getTag());
		}
		if(stack.getTag().isEmpty()) {
			stack.setTag(null);
		}
	}

	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if(!worldIn.isClientSide && playerIn.isShiftKeyDown() && isFilled(playerIn.getItemInHand(handIn))) {
			ItemStack stack = playerIn.getItemInHand(handIn).copy();
			setRecipeList(stack, null);
			return ActionResult.success(stack);
		}
		return super.use(worldIn, playerIn, handIn);
	}

	@Override
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if(stack.hasTag()) {
			List<IPackageRecipeInfo> recipeList = getRecipeList(stack).getRecipeList();
			tooltip.add(new TranslationTextComponent("item.packagedauto.recipe_holder.recipes"));
			for(IPackageRecipeInfo recipe : recipeList) {
				IFormattableTextComponent component = recipe.getRecipeType().getDisplayName().append(": ");
				for(int i = 0; i < recipe.getOutputs().size(); ++i) {
					if(i != 0) {
						component.append(", ");
					}
					ItemStack is = recipe.getOutputs().get(i);
					component.append(is.getCount()+" ").append(is.getDisplayName());
				}
				tooltip.add(component);
			}
		}
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
	}

	public boolean isFilled(ItemStack stack) {
		CompoundNBT nbt = stack.getTag();
		return nbt != null && !nbt.getList("Recipes", 10).isEmpty();
	}
}
