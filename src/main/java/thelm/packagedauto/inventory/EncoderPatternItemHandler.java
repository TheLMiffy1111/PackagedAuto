package thelm.packagedauto.inventory;

import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.recipe.ProcessingPackageRecipeType;
import thelm.packagedauto.tile.EncoderTile;
import thelm.packagedauto.util.ApiImpl;

public class EncoderPatternItemHandler extends BaseItemHandler {

	public final EncoderTile tile;
	public IPackageRecipeType recipeType;
	public IPackageRecipeInfo recipeInfo;

	public EncoderPatternItemHandler(EncoderTile tile) {
		super(tile, 99);
		this.tile = tile;
		validateRecipeType();
	}

	@Override
	public void onContentsChanged(int slot) {
		//maybe add check to see where this is called
		updateRecipeInfo();
	}

	@Override
	public void read(CompoundNBT nbt) {
		super.read(nbt);
		recipeType = ApiImpl.INSTANCE.getRecipeType(new ResourceLocation(nbt.getString("RecipeType")));
		validateRecipeType();
		updateRecipeInfo();
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		super.write(nbt);
		validateRecipeType();
		nbt.putString("RecipeType", recipeType.getName().toString());
		return nbt;
	}

	@Override
	public boolean isItemValid(int index, ItemStack stack) {
		validateRecipeType();
		if(index < 81 || recipeType.canSetOutput() && index < 90) {
			return recipeType.getEnabledSlots().contains(index);
		}
		return false;
	}

	public void validateRecipeType() {
		if(recipeType == null) {
			recipeType = ProcessingPackageRecipeType.INSTANCE;
		}
	}

	public void updateRecipeInfo() {
		validateRecipeType();
		IPackageRecipeInfo info = recipeType.getNewRecipeInfo();
		info.generateFromStacks(stacks.subList(0, 81), recipeType.canSetOutput() ? stacks.subList(81, 90) : Collections.emptyList(), tile.getWorld());
		if(info.isValid()) {
			if(recipeInfo == null || !recipeInfo.equals(info)) {
				recipeInfo = info;
				if(!recipeType.canSetOutput()) {
					for(int i = 81; i < 90; ++i) {
						stacks.set(i, ItemStack.EMPTY);
					}
					List<ItemStack> outputs = info.getOutputs();
					int size = outputs.size();
					int startIndex = 81;
					switch(size) {
					case 1: startIndex += 1;
					case 2:
					case 3: startIndex += 3;
					}
					for(int i = 0; i < size; ++i) {
						stacks.set(startIndex+i, outputs.get(i).copy());
					}
				}
				for(int i = 90; i < 99; ++i) {
					stacks.set(i, ItemStack.EMPTY);
				}
				List<IPackagePattern> patterns = info.getPatterns();
				for(int i = 0; i < patterns.size() && i < 9; ++i) {
					stacks.set(90+i, patterns.get(i).getOutput().copy());
				}
				syncTile(false);
				markDirty();
			}
		}
		else if(recipeInfo != null) {
			recipeInfo = null;
			if(!recipeType.canSetOutput()) {
				for(int i = 81; i < 90; ++i) {
					stacks.set(i, ItemStack.EMPTY);
				}
			}
			for(int i = 90; i < 99; ++i) {
				stacks.set(i, ItemStack.EMPTY);
			}
			syncTile(false);
			markDirty();
		}
	}

	public void cycleRecipeType(boolean reverse) {
		validateRecipeType();
		recipeType = ApiImpl.INSTANCE.getNextRecipeType(recipeType, reverse);
		validateRecipeType();
		IntSet enabledSlots = recipeType.getEnabledSlots();
		for(int i = 0; i < 90; ++i) {
			if(!enabledSlots.contains(i)) {
				stacks.set(i, ItemStack.EMPTY);
			}
		}
		updateRecipeInfo();
	}

	public void setRecipe(Int2ObjectMap<ItemStack> map) {
		if(recipeType.canSetOutput()) {
			stacks.clear();
		}
		else {
			for(int i = 0; i < 81; ++i) {
				stacks.set(i, ItemStack.EMPTY);
			}
		}
		if(map != null) {
			for(Int2ObjectMap.Entry<ItemStack> entry : map.int2ObjectEntrySet()) {
				stacks.set(entry.getIntKey(), entry.getValue());
			}
		}
		updateRecipeInfo();
	}
}
