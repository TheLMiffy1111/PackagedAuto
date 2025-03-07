package thelm.packagedauto.tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeList;
import thelm.packagedauto.api.IPackageRecipeListItem;
import thelm.packagedauto.block.EncoderBlock;
import thelm.packagedauto.container.EncoderContainer;
import thelm.packagedauto.inventory.EncoderItemHandler;
import thelm.packagedauto.inventory.EncoderPatternItemHandler;

public class EncoderTile extends BaseTile {

	public static final TileEntityType<EncoderTile> TYPE_INSTANCE = (TileEntityType<EncoderTile>)TileEntityType.Builder.
			of(EncoderTile::new, EncoderBlock.INSTANCE).
			build(null).setRegistryName("packagedauto:encoder");

	public static int patternSlots = 20;
	public static Set<String> disabledRecipeTypes = Collections.emptySet();

	public final EncoderPatternItemHandler[] patternItemHandlers = new EncoderPatternItemHandler[patternSlots];
	public int patternIndex;

	public EncoderTile() {
		super(TYPE_INSTANCE);
		setItemHandler(new EncoderItemHandler(this));
		for(int i = 0; i < patternItemHandlers.length; ++i) {
			patternItemHandlers[i] = new EncoderPatternItemHandler(this);
		}
	}

	@Override
	protected ITextComponent getDefaultName() {
		return new TranslationTextComponent("block.packagedauto.encoder");
	}

	@Override
	public void setLevelAndPosition(World world, BlockPos pos) {
		super.setLevelAndPosition(world, pos);
		for(EncoderPatternItemHandler inv : patternItemHandlers) {
			inv.updateRecipeInfo(false);
		}
	}

	@Override
	public void readSync(CompoundNBT nbt) {
		super.readSync(nbt);
		patternIndex = nbt.getByte("PatternIndex");
		for(int i = 0; i < patternItemHandlers.length; ++i) {
			patternItemHandlers[i].read(nbt.getCompound(String.format("Pattern%02d", i)));
		}
	}

	@Override
	public CompoundNBT writeSync(CompoundNBT nbt) {
		super.writeSync(nbt);
		nbt.putByte("PatternIndex", (byte)patternIndex);
		for(int i = 0; i < patternItemHandlers.length; ++i) {
			CompoundNBT subNBT = new CompoundNBT();
			patternItemHandlers[i].write(subNBT);
			nbt.put(String.format("Pattern%02d", i), subNBT);
		}
		return nbt;
	}

	public void setPatternIndex(int patternIndex) {
		this.patternIndex = patternIndex;
		syncTile(false);
		setChanged();
	}

	public void saveRecipeList(boolean single) {
		ItemStack stack = itemHandler.getStackInSlot(0);
		if(stack.getItem() instanceof IPackageRecipeListItem) {
			IPackageRecipeListItem recipeListItem = (IPackageRecipeListItem)stack.getItem();
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
		if(stack.getItem() instanceof IPackageRecipeListItem) {
			IPackageRecipeList recipeListObj = ((IPackageRecipeListItem)stack.getItem()).getRecipeList(stack);
			List<IPackageRecipeInfo> recipeList = recipeListObj.getRecipeList();
			if(single) {
				EncoderPatternItemHandler inv = patternItemHandlers[patternIndex];
				if(!clear && !recipeList.isEmpty()) {
					IPackageRecipeInfo recipe = recipeList.get(0);
					inv.recipeType = recipe.getRecipeType();
					if(recipe.isValid()) {
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
					if(recipe.isValid()) {
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
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) {
		syncTile(false);
		return new EncoderContainer(windowId, playerInventory, this);
	}
}
