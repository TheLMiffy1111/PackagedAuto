package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.IRecipeType;
import thelm.packagedauto.api.RecipeTypeRegistry;
import thelm.packagedauto.client.IModelRegister;

public class ItemPackage extends Item implements IPackageItem, IModelRegister {

	public static final ItemPackage INSTANCE = new ItemPackage();
	public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation("packagedauto:package#inventory");

	protected ItemPackage() {
		setRegistryName("packagedauto:package");
		setUnlocalizedName("packagedauto.package");
		setCreativeTab(null);
	}

	public static ItemStack makePackage(IRecipeInfo recipeInfo, int index) {
		ItemStack stack = new ItemStack(INSTANCE);
		NBTTagCompound tag = recipeInfo.writeToNBT(new NBTTagCompound());
		tag.setString("RecipeType", recipeInfo.getRecipeType().getName().toString());
		tag.setByte("Index", (byte)index);
		stack.setTagCompound(tag);
		return stack;
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
			IRecipeType recipeType = RecipeTypeRegistry.getRecipeType(new ResourceLocation(tag.getString("RecipeType")));
			if(recipeType == null) {
				return null;
			}
			IRecipeInfo recipe = recipeType.getNewRecipeInfo();
			recipe.readFromNBT(tag);
			if(recipe.isValid()) {
				return recipe;
			}
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
