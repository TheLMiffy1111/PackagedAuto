package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.util.MiscHelper;

public class PackageItem extends Item implements IPackageItem {

	public static final PackageItem INSTANCE = new PackageItem();

	protected PackageItem() {
		super(new Item.Properties());
		setRegistryName("packagedauto:package");
	}

	public static ItemStack makePackage(IPackageRecipeInfo recipeInfo, int index) {
		ItemStack stack = new ItemStack(INSTANCE);
		CompoundNBT tag = MiscHelper.INSTANCE.writeRecipe(new CompoundNBT(), recipeInfo);
		tag.putByte("Index", (byte)index);
		stack.setTag(tag);
		return stack;
	}

	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if(!worldIn.isClientSide && playerIn.isShiftKeyDown()) {
			ItemStack stack = playerIn.getItemInHand(handIn).copy();
			ItemStack stack1 = stack.split(1);
			IPackageRecipeInfo recipeInfo = getRecipeInfo(stack1);
			if(recipeInfo != null) {
				List<IPackagePattern> patterns = recipeInfo.getPatterns();
				int index = getIndex(stack1);
				if(index >= 0 && index < patterns.size()) {
					IPackagePattern pattern = patterns.get(index);
					List<ItemStack> inputs = pattern.getInputs();
					for(int i = 0; i < inputs.size(); ++i) {
						ItemStack input = inputs.get(i).copy();
						if(!playerIn.inventory.add(input)) {
							ItemEntity item = new ItemEntity(worldIn, playerIn.getX(), playerIn.getY(), playerIn.getZ(), input);
							item.setThrower(playerIn.getUUID());
							worldIn.addFreshEntity(item);
						}
					}
				}
			}
			return new ActionResult<>(ActionResultType.SUCCESS, stack);
		}
		return new ActionResult<>(ActionResultType.PASS, playerIn.getItemInHand(handIn));
	}

	@Override
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		IPackageRecipeInfo recipe = getRecipeInfo(stack);
		if(recipe != null) {
			tooltip.add(recipe.getRecipeType().getDisplayName().append(": "));
			for(ItemStack is : recipe.getOutputs()) {
				tooltip.add(new StringTextComponent(is.getCount()+" ").append(is.getDisplayName()));
			}
			int index = getIndex(stack);
			tooltip.add(new TranslationTextComponent("item.packagedauto.package.index", index));
			tooltip.add(new TranslationTextComponent("item.packagedauto.package.items"));
			List<ItemStack> recipeInputs = recipe.getInputs();
			List<ItemStack> packageItems = recipeInputs.subList(9*index, Math.min(9*index+9, recipeInputs.size()));
			for(ItemStack is : packageItems) {
				tooltip.add(new StringTextComponent(is.getCount()+" ").append(is.getDisplayName()));
			}
		}
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
	}

	@Override
	public IPackageRecipeInfo getRecipeInfo(ItemStack stack) {
		if(stack.hasTag()) {
			CompoundNBT tag = stack.getTag();
			return MiscHelper.INSTANCE.readRecipe(tag);
		}
		return null;
	}

	@Override
	public int getIndex(ItemStack stack) {
		if(stack.hasTag()) {
			return stack.getTag().getByte("Index");
		}
		return -1;
	}
}
