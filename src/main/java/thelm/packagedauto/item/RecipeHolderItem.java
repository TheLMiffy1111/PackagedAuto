package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
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
		super(new Item.Properties().group(PackagedAuto.ITEM_GROUP));
		setRegistryName("packagedauto:recipe_holder");
	}

	@Override
	public IPackageRecipeList getRecipeList(World world, ItemStack stack) {
		return new PackageRecipeList(world, stack.getTag());
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if(!worldIn.isRemote && playerIn.isSneaking()) {
			return new ActionResult<>(ActionResultType.SUCCESS, new ItemStack(INSTANCE, playerIn.getHeldItem(handIn).getCount()));
		}
		return new ActionResult<>(ActionResultType.PASS, playerIn.getHeldItem(handIn));
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if(stack.hasTag()) {
			List<IPackageRecipeInfo> recipeList = getRecipeList(worldIn, stack).getRecipeList();
			tooltip.add(new TranslationTextComponent("item.packagedauto.recipe_holder.recipes"));
			for(IPackageRecipeInfo recipe : recipeList) {
				IFormattableTextComponent component = recipe.getRecipeType().getDisplayName().appendString(": ");
				for(int i = 0; i < recipe.getOutputs().size(); ++i) {
					if(i != 0) {
						component.appendString(", ");
					}
					ItemStack is = recipe.getOutputs().get(i);
					component.appendString(is.getCount()+" ").appendSibling(is.getDisplayName());
				}
				tooltip.add(component);
			}
		}
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}
}
