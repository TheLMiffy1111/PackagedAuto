package thelm.packagedauto.item;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeList;
import thelm.packagedauto.api.IPackageRecipeListItem;
import thelm.packagedauto.util.PackageRecipeList;

public class ItemRecipeHolder extends Item implements IPackageRecipeListItem {

	public static final ItemRecipeHolder INSTANCE = new ItemRecipeHolder();

	@SideOnly(Side.CLIENT)
	protected IIcon filledIcon;

	public ItemRecipeHolder() {
		setUnlocalizedName("packagedauto.recipe_holder");
		setCreativeTab(PackagedAuto.CREATIVE_TAB);
	}

	@Override
	public IPackageRecipeList getRecipeList(ItemStack stack) {
		return new PackageRecipeList(stack.getTagCompound());
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(!world.isRemote && player.isSneaking()) {
			return new ItemStack(INSTANCE, stack.stackSize);
		}
		return stack;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean isAdvanced) {
		if(stack.hasTagCompound()) {
			List<IPackageRecipeInfo> recipeList = getRecipeList(stack).getRecipeList();
			tooltip.add(StatCollector.translateToLocal("item.packagedauto.recipe_holder.recipes"));
			for(IPackageRecipeInfo recipe : recipeList) {
				StringBuilder sb = new StringBuilder();
				sb.append(recipe.getRecipeType().getLocalizedName()).append(": ");
				List<String> stackNames = recipe.getOutputs().stream().map(is->is.stackSize+" "+is.getDisplayName()).collect(Collectors.toList());
				sb.append(StringUtils.abbreviate(StringUtils.join(stackNames, ", "), 64));
				tooltip.add(sb.toString());
			}
		}
		super.addInformation(stack, player, tooltip, isAdvanced);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister register) {
		itemIcon = register.registerIcon("packagedauto:recipe_holder");
		filledIcon = register.registerIcon("packagedauto:recipe_holder_filled");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIconIndex(ItemStack stack) {
		return stack.hasTagCompound() ? filledIcon : itemIcon;
	}
}
