package thelm.packagedauto.tile;

import java.util.ArrayList;
import java.util.List;

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
import thelm.packagedauto.recipe.ProcessingPackageRecipeType;

public class EncoderTile extends BaseTile {

	public static final TileEntityType<EncoderTile> TYPE_INSTANCE = (TileEntityType<EncoderTile>)TileEntityType.Builder.
			create(EncoderTile::new, EncoderBlock.INSTANCE).
			build(null).setRegistryName("packagedauto:encoder");

	public static int patternSlots = 20;

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
	public void setWorldAndPos(World world, BlockPos pos) {
		super.setWorldAndPos(world, pos);
		for(EncoderPatternItemHandler inv : patternItemHandlers) {
			inv.updateRecipeInfo();
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
		markDirty();
	}

	public void saveRecipeList(boolean single) {
		ItemStack stack = itemHandler.getStackInSlot(0);
		if(stack.getItem() instanceof IPackageRecipeListItem) {
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
			IPackageRecipeList recipeListItem = ((IPackageRecipeListItem)stack.getItem()).getRecipeList(world, stack);
			recipeListItem.setRecipeList(recipeList);
			CompoundNBT nbt = recipeListItem.write(new CompoundNBT());
			itemHandler.getStackInSlot(0).setTag(nbt);
		}
	}

	public void loadRecipeList() {
		ItemStack stack = itemHandler.getStackInSlot(0);
		if(stack.getItem() instanceof IPackageRecipeListItem) {
			IPackageRecipeList recipeListItem = ((IPackageRecipeListItem)stack.getItem()).getRecipeList(world, stack);
			List<IPackageRecipeInfo> recipeList = recipeListItem.getRecipeList();
			for(int i = 0; i < patternItemHandlers.length; ++i) {
				EncoderPatternItemHandler inv = patternItemHandlers[i];
				if(i < recipeList.size()) {
					IPackageRecipeInfo recipe = recipeList.get(i);
					inv.recipeType = recipe.getRecipeType();
					inv.setRecipe(recipe.getEncoderStacks());
				}
				else {
					inv.recipeType = ProcessingPackageRecipeType.INSTANCE;
					inv.setRecipe(null);
				}
			}
		}
		else {
			for(EncoderPatternItemHandler inv : patternItemHandlers) {
				inv.recipeType = ProcessingPackageRecipeType.INSTANCE;
				inv.setRecipe(null);
			}
		}
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) {
		syncTile(false);
		return new EncoderContainer(windowId, playerInventory, this);
	}
}
