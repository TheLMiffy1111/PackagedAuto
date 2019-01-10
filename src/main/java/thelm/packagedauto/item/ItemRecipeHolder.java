package thelm.packagedauto.item;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.IRecipeList;
import thelm.packagedauto.api.IRecipeListItem;
import thelm.packagedauto.client.IModelRegister;
import thelm.packagedauto.util.RecipeListHelper;

public class ItemRecipeHolder extends Item implements IRecipeListItem, IModelRegister {

	public static final ItemRecipeHolder INSTANCE = new ItemRecipeHolder();
	public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation("packagedauto:recipe_holder#inventory");
	public static final ModelResourceLocation MODEL_LOCATION_FILLED = new ModelResourceLocation("packagedauto:recipe_holder_filled#inventory");

	public ItemRecipeHolder() {
		setUnlocalizedName("packagedauto.recipe_holder");
		setRegistryName("packagedauto:recipe_holder");
		setCreativeTab(PackagedAuto.CREATIVE_TAB);
	}

	@Override
	public IRecipeList getRecipeList(ItemStack stack) {
		return new RecipeListHelper(stack.getTagCompound());
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if(!worldIn.isRemote && playerIn.isSneaking()) {
			return new ActionResult<>(EnumActionResult.SUCCESS, new ItemStack(INSTANCE, playerIn.getHeldItem(handIn).getCount()));
		}
		return new ActionResult<>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if(stack.hasTagCompound()) {
			List<IRecipeInfo> recipeList = getRecipeList(stack).getRecipeList();
			tooltip.add(I18n.translateToLocal("item.packagedauto.recipe_holder.recipes"));
			for(IRecipeInfo recipe : recipeList) {
				StringBuilder sb = new StringBuilder();
				sb.append(recipe.getRecipeType().getLocalizedName()).append(": ");
				List<String> stackNames = recipe.getOutputs().stream().map(is->is.getCount()+" "+is.getDisplayName()).collect(Collectors.toList());
				sb.append(StringUtils.abbreviate(StringUtils.join(stackNames, ", "), 64));
				tooltip.add(sb.toString());
			}
		}
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels() {
		ModelLoader.setCustomMeshDefinition(this, stack->stack.hasTagCompound() ? MODEL_LOCATION_FILLED : MODEL_LOCATION);
		ModelBakery.registerItemVariants(this, MODEL_LOCATION, MODEL_LOCATION_FILLED);
	}
}
