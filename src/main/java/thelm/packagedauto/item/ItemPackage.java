package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.util.MiscHelper;

public class ItemPackage extends Item implements IPackageItem {

	public static final ItemPackage INSTANCE = new ItemPackage();

	protected ItemPackage() {
		setUnlocalizedName("packagedauto.package");
		setTextureName("packagedauto:package");
		setCreativeTab(null);
	}

	public static ItemStack makePackage(IPackageRecipeInfo recipeInfo, int index) {
		ItemStack stack = new ItemStack(INSTANCE);
		NBTTagCompound tag = MiscHelper.INSTANCE.writeRecipeToNBT(new NBTTagCompound(), recipeInfo);
		tag.setByte("Index", (byte)index);
		stack.setTagCompound(tag);
		return stack;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
		if(!world.isRemote && player.isSneaking()) {
			ItemStack stack = itemStack.copy();
			ItemStack stack1 = stack.splitStack(1);
			IPackageRecipeInfo recipeInfo = getRecipeInfo(stack1);
			if(recipeInfo != null) {
				List<IPackagePattern> patterns = recipeInfo.getPatterns();
				int index = getIndex(stack1);
				if(index >= 0 && index < patterns.size()) {
					IPackagePattern pattern = patterns.get(index);
					List<ItemStack> inputs = pattern.getInputs();
					for(int i = 0; i < inputs.size(); ++i) {
						ItemStack input = inputs.get(i).copy();
						if(!player.inventory.addItemStackToInventory(input)) {
							EntityItem item = new EntityItem(world, player.posX, player.posY, player.posZ, input);
							item.func_145799_b(player.getCommandSenderName());
							world.spawnEntityInWorld(item);
						}
					}
				}
			}
			return stack;
		}
		return itemStack;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean isAdvanced) {
		IPackageRecipeInfo recipe = getRecipeInfo(stack);
		if(recipe != null) {
			tooltip.add(recipe.getRecipeType().getLocalizedName()+": ");
			for(ItemStack is : recipe.getOutputs()) {
				tooltip.add(is.stackSize+" "+is.getDisplayName());
			}
			int index = getIndex(stack);
			tooltip.add(StatCollector.translateToLocalFormatted("item.packagedauto.package.index", index));
			tooltip.add(StatCollector.translateToLocal("item.packagedauto.package.items"));
			List<ItemStack> recipeInputs = recipe.getInputs();
			List<ItemStack> packageItems = recipeInputs.subList(9*index, Math.min(9*index+9, recipeInputs.size()));
			for(ItemStack is : packageItems) {
				tooltip.add(is.stackSize+" "+is.getDisplayName());
			}
		}
		super.addInformation(stack, player, tooltip, isAdvanced);
	}

	@Override
	public IPackageRecipeInfo getRecipeInfo(ItemStack stack) {
		if(stack.hasTagCompound()) {
			NBTTagCompound tag = stack.getTagCompound();
			return MiscHelper.INSTANCE.readRecipeFromNBT(tag);
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
}
