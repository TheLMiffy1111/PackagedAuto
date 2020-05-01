package thelm.packagedauto.item;

import java.util.List;

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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.client.IModelRegister;

public class ItemPackage extends Item implements IPackageItem, IModelRegister {

	public static final ItemPackage INSTANCE = new ItemPackage();
	public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation("packagedauto:package#inventory");

	protected ItemPackage() {
		setRegistryName("packagedauto:package");
		setTranslationKey("packagedauto.package");
		setCreativeTab(null);
	}

	public static ItemStack makePackage(IRecipeInfo recipeInfo, int index) {
		ItemStack stack = new ItemStack(INSTANCE);
		NBTTagCompound tag = MiscUtil.writeRecipeToNBT(new NBTTagCompound(), recipeInfo);
		tag.setByte("Index", (byte)index);
		stack.setTagCompound(tag);
		return stack;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if(!worldIn.isRemote && playerIn.isSneaking()) {
			ItemStack stack = playerIn.getHeldItem(handIn).copy();
			ItemStack stack1 = stack.splitStack(1);
			IRecipeInfo recipeInfo = getRecipeInfo(stack1);
			if(recipeInfo != null) {
				List<IPackagePattern> patterns = recipeInfo.getPatterns();
				int index = getIndex(stack1);
				if(index >= 0 && index < patterns.size()) {
					IPackagePattern pattern = patterns.get(index);
					List<ItemStack> inputs = pattern.getInputs();
					for(int i = 0; i < inputs.size(); ++i) {
						ItemStack input = inputs.get(i).copy();
						if(!playerIn.inventory.addItemStackToInventory(input)) {
							EntityItem item = new EntityItem(worldIn, playerIn.posX, playerIn.posY, playerIn.posZ, input);
							item.setThrower(playerIn.getName());
							worldIn.spawnEntity(item);
						}
					}
				}
			}
			return new ActionResult<>(EnumActionResult.SUCCESS, stack);
		}
		return new ActionResult<>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		IRecipeInfo recipe = getRecipeInfo(stack);
		if(recipe != null) {
			tooltip.add(recipe.getRecipeType().getLocalizedName()+": ");
			for(ItemStack is : recipe.getOutputs()) {
				tooltip.add(is.getCount()+" "+is.getDisplayName());
			}
			int index = getIndex(stack);
			tooltip.add(I18n.translateToLocalFormatted("item.packagedauto.package.index", index));
			tooltip.add(I18n.translateToLocal("item.packagedauto.package.items"));
			List<ItemStack> recipeInputs = recipe.getInputs();
			List<ItemStack> packageItems = recipeInputs.subList(9*index, Math.min(9*index+9, recipeInputs.size()));
			for(ItemStack is : packageItems) {
				tooltip.add(is.getCount()+" "+is.getDisplayName());
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

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels() {
		ModelLoader.setCustomModelResourceLocation(this, 0, MODEL_LOCATION);
	}
}
