package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
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
		CompoundTag tag = MiscHelper.INSTANCE.saveRecipe(new CompoundTag(), recipeInfo);
		tag.putByte("Index", (byte)index);
		stack.setTag(tag);
		return stack;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if(!level.isClientSide && player.isShiftKeyDown()) {
			ItemStack stack = player.getItemInHand(hand).copy();
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
						if(!player.getInventory().add(input)) {
							ItemEntity item = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), input);
							item.setThrower(player.getUUID());
							level.addFreshEntity(item);
						}
					}
				}
			}
			return InteractionResultHolder.success(stack);
		}
		return InteractionResultHolder.pass(player.getItemInHand(hand));
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
		IPackageRecipeInfo recipe = getRecipeInfo(stack);
		if(recipe != null) {
			tooltip.add(recipe.getRecipeType().getDisplayName().append(": "));
			for(ItemStack is : recipe.getOutputs()) {
				tooltip.add(new TextComponent(is.getCount()+" ").append(is.getDisplayName()));
			}
			int index = getIndex(stack);
			tooltip.add(new TranslatableComponent("item.packagedauto.package.index", index));
			tooltip.add(new TranslatableComponent("item.packagedauto.package.items"));
			List<ItemStack> recipeInputs = recipe.getInputs();
			List<ItemStack> packageItems = recipeInputs.subList(9*index, Math.min(9*index+9, recipeInputs.size()));
			for(ItemStack is : packageItems) {
				tooltip.add(new TextComponent(is.getCount()+" ").append(is.getDisplayName()));
			}
		}
		super.appendHoverText(stack, level, tooltip, isAdvanced);
	}

	@Override
	public IPackageRecipeInfo getRecipeInfo(ItemStack stack) {
		if(stack.hasTag()) {
			CompoundTag tag = stack.getTag();
			return MiscHelper.INSTANCE.loadRecipe(tag);
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
