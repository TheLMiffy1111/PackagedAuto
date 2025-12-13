package thelm.packagedauto.block.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeList;
import thelm.packagedauto.api.IPackageRecipeListItem;
import thelm.packagedauto.block.EncoderBlock;
import thelm.packagedauto.inventory.EncoderItemHandler;
import thelm.packagedauto.inventory.EncoderPatternItemHandler;
import thelm.packagedauto.menu.EncoderMenu;

public class EncoderBlockEntity extends BaseBlockEntity {

	public static final BlockEntityType<EncoderBlockEntity> TYPE_INSTANCE = BlockEntityType.Builder.
			of(EncoderBlockEntity::new, EncoderBlock.INSTANCE).build(null);

	public static int patternSlots = 20;
	public static Set<String> disabledRecipeTypes = Set.of();

	public final EncoderPatternItemHandler[] patternItemHandlers = new EncoderPatternItemHandler[patternSlots];
	public int patternIndex;

	public EncoderBlockEntity(BlockPos pos, BlockState state) {
		super(TYPE_INSTANCE, pos, state);
		setItemHandler(new EncoderItemHandler(this));
		for(int i = 0; i < patternItemHandlers.length; ++i) {
			patternItemHandlers[i] = new EncoderPatternItemHandler(this);
		}
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("block.packagedauto.encoder");
	}

	@Override
	public void setLevel(Level level) {
		super.setLevel(level);
		for(EncoderPatternItemHandler inv : patternItemHandlers) {
			inv.updateRecipeInfo(false);
		}
	}

	@Override
	public void loadSync(CompoundTag nbt) {
		super.loadSync(nbt);
		patternIndex = nbt.getByte("PatternIndex");
		for(int i = 0; i < patternItemHandlers.length; ++i) {
			patternItemHandlers[i].load(nbt.getCompound(String.format("Pattern%02d", i)));
		}
	}

	@Override
	public CompoundTag saveSync(CompoundTag nbt) {
		super.saveSync(nbt);
		nbt.putByte("PatternIndex", (byte)patternIndex);
		for(int i = 0; i < patternItemHandlers.length; ++i) {
			CompoundTag subNBT = new CompoundTag();
			patternItemHandlers[i].save(subNBT);
			nbt.put(String.format("Pattern%02d", i), subNBT);
		}
		return nbt;
	}

	public void setPatternIndex(int patternIndex) {
		this.patternIndex = patternIndex;
		sync(false);
		setChanged();
	}

	public void saveRecipeList(boolean single) {
		ItemStack stack = itemHandler.getStackInSlot(0);
		if(stack.getItem() instanceof IPackageRecipeListItem recipeListItem) {
			IPackageRecipeList recipeListObj = recipeListItem.getRecipeList(stack);
			List<IPackageRecipeInfo> recipeList = new ArrayList<>();
			if(!single) {
				for(EncoderPatternItemHandler inv : patternItemHandlers) {
					if(inv.recipeInfo != null) {
						recipeList.add(inv.recipeInfo);
					}
				}
			}
			else {
				EncoderPatternItemHandler inv = patternItemHandlers[patternIndex];
				if(inv.recipeInfo != null) {
					recipeList.add(inv.recipeInfo);
				}
			}
			recipeListObj.setRecipeList(recipeList);
			recipeListItem.setRecipeList(stack, recipeListObj);
		}
	}

	public void loadRecipeList(boolean single, boolean clear) {
		ItemStack stack = itemHandler.getStackInSlot(0);
		if(stack.getItem() instanceof IPackageRecipeListItem recipeListItem) {
			IPackageRecipeList recipeListObj = recipeListItem.getRecipeList(stack);
			List<IPackageRecipeInfo> recipeList = recipeListObj.getRecipeList();
			if(single) {
				EncoderPatternItemHandler inv = patternItemHandlers[patternIndex];
				if(!clear && !recipeList.isEmpty()) {
					IPackageRecipeInfo recipe = recipeList.get(0);
					if(recipe.isPackageable()) {
						inv.setRecipe(recipe.getEncoderStacks());
					}
				}
				else {
					inv.setRecipe(null);
				}
			}
			else for(int i = 0; i < patternItemHandlers.length; ++i) {
				EncoderPatternItemHandler inv = patternItemHandlers[i];
				if(!clear && i < recipeList.size()) {
					IPackageRecipeInfo recipe = recipeList.get(i);
					inv.recipeType = recipe.getRecipeType();
					if(recipe.isPackageable()) {
						inv.setRecipe(recipe.getEncoderStacks());
					}
				}
				else {
					inv.setRecipe(null);
				}
			}
		}
		else if(single) {
			patternItemHandlers[patternIndex].setRecipe(null);
		}
		else for(EncoderPatternItemHandler inv : patternItemHandlers) {
			inv.setRecipe(null);
		}
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
		sync(false);
		return new EncoderMenu(windowId, inventory, this);
	}
}
