package thelm.packagedauto.item;

import java.util.List;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.api.PatternType;
import thelm.packagedauto.client.IModelRegister;
import thelm.packagedauto.integration.appeng.recipe.PackageCraftingPatternHelper;
import thelm.packagedauto.integration.appeng.recipe.RecipeCraftingPatternHelper;

@Optional.InterfaceList({
	@Optional.Interface(iface="appeng.api.implementations.ICraftingPatternItem", modid="appliedenergistics2"),
})
public class ItemPackage extends Item implements IPackageItem, IModelRegister, ICraftingPatternItem {

	public static final ItemPackage INSTANCE = new ItemPackage();
	public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation("packagedauto:package#inventory");

	protected ItemPackage() {
		setTranslationKey("packagedauto.package");
		setRegistryName("packagedauto:package");
		setCreativeTab(null);
	}

	public static ItemStack makePackage(IRecipeInfo recipeInfo, int index) {
		ItemStack stack = new ItemStack(INSTANCE);
		if(recipeInfo != null) {
			NBTTagCompound tag = MiscUtil.writeRecipeToNBT(new NBTTagCompound(), recipeInfo);
			tag.setByte("Index", (byte)index);
			stack.setTagCompound(tag);
		}
		return stack;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if(!worldIn.isRemote && playerIn.isSneaking()) {
			ItemStack stack = playerIn.getHeldItem(handIn).copy();
			ItemStack stack1 = stack.splitStack(1);
			IRecipeInfo recipe = getRecipeInfo(stack1);
			int index = getIndex(stack1);
			if(recipe != null && recipe.validPatternIndex(index)) {
				List<ItemStack> inputs = recipe.getPatterns().get(index).getInputs();
				for(int i = 0; i < inputs.size(); ++i) {
					ItemStack input = inputs.get(i).copy();
					if(!playerIn.inventory.addItemStackToInventory(input)) {
						EntityItem item = new EntityItem(worldIn, playerIn.posX, playerIn.posY, playerIn.posZ, input);
						item.setThrower(playerIn.getName());
						worldIn.spawnEntity(item);
					}
				}
			}
			return new ActionResult<>(EnumActionResult.SUCCESS, stack);
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		IRecipeInfo recipe = getRecipeInfo(stack);
		int index = getIndex(stack);
		if(recipe != null && recipe.validPatternIndex(index)) {
			tooltip.add(recipe.getRecipeType().getLocalizedName()+": ");
			for(ItemStack is : recipe.getOutputs()) {
				tooltip.add(is.getCount()+" "+is.copy().getDisplayName());
			}
			tooltip.add(I18n.translateToLocalFormatted("item.packagedauto.package.index", index));
			tooltip.add(I18n.translateToLocal("item.packagedauto.package.items"));
			List<ItemStack> recipeInputs = recipe.getInputs();
			List<ItemStack> packageItems = recipeInputs.subList(9*index, Math.min(9*index+9, recipeInputs.size()));
			for(ItemStack is : packageItems) {
				tooltip.add(is.getCount()+" "+is.copy().getDisplayName());
			}
		}
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}

	@Override
	public IRecipeInfo getRecipeInfo(ItemStack stack) {
		if(stack.hasTagCompound()) {
			NBTTagCompound tag = stack.getTagCompound();
			return MiscUtil.readRecipeFromNBT(tag);
		}
		return null;
	}

	@Override
	public int getIndex(ItemStack stack) {
		if(stack.hasTagCompound()) {
			return stack.getTagCompound().getByte("Index");
		}
		return -1;
	}

	@Override
	public PatternType getPatternType(ItemStack stack) {
		if(stack.hasTagCompound()) {
			return PatternType.fromName(stack.getTagCompound().getString("PatternType"));
		}
		return null;
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public ICraftingPatternDetails getPatternForItem(ItemStack stack, World world) {
		PatternType patternType = getPatternType(stack);
		if(patternType != null) {
			switch(getPatternType(stack)) {
			case PACKAGE: {
				IRecipeInfo recipe = getRecipeInfo(stack);
				int index = getIndex(stack);
				if(recipe != null && recipe.isValid() && recipe.validPatternIndex(index)) {
					return new PackageCraftingPatternHelper(recipe.getPatterns().get(index));
				}
				break;
			}
			case RECIPE: {
				IRecipeInfo recipe = getRecipeInfo(stack);
				if(recipe != null && recipe.isValid()) {
					return new RecipeCraftingPatternHelper(recipe);
				}
				break;
			}
			}
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels() {
		ModelLoader.setCustomModelResourceLocation(this, 0, MODEL_LOCATION);
	}
}
