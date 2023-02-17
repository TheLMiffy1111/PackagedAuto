package thelm.packagedauto.inventory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.recipe.PackageRecipeTypeProcessing;
import thelm.packagedauto.tile.TileEncoder;
import thelm.packagedauto.util.ApiImpl;

public class InventoryEncoderPattern extends InventoryBase {

	public final TileEncoder tile;
	public IPackageRecipeType recipeType;
	public IPackageRecipeInfo recipeInfo;

	public InventoryEncoderPattern(TileEncoder tile) {
		super(tile, 99);
		this.tile = tile;
		validateRecipeType();
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		super.setInventorySlotContents(index, stack);
		//maybe add check to see where this is called
		updateRecipeInfo(true);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		recipeType = ApiImpl.INSTANCE.getRecipeType(nbt.getString("RecipeType"));
		validateRecipeType();
		updateRecipeInfo(false);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		validateRecipeType();
		nbt.setString("RecipeType", recipeType.getName().toString());
		return nbt;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		validateRecipeType();
		if(index < 81 || recipeType.canSetOutput() && index < 90) {
			return recipeType.getEnabledSlots().contains(index);
		}
		return false;
	}

	public void validateRecipeType() {
		if(recipeType == null) {
			recipeType = PackageRecipeTypeProcessing.INSTANCE;
		}
	}

	public void updateRecipeInfo(boolean mark) {
		validateRecipeType();
		IPackageRecipeInfo info = recipeType.getNewRecipeInfo();
		info.generateFromStacks(stacks.subList(0, 81), recipeType.canSetOutput() ? stacks.subList(81, 90) : Collections.emptyList(), tile.getWorldObj());
		if(info.isValid()) {
			if(recipeInfo == null || !recipeInfo.equals(info)) {
				recipeInfo = info;
				if(!recipeType.canSetOutput()) {
					for(int i = 81; i < 90; ++i) {
						stacks.set(i, null);
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
					stacks.set(i, null);
				}
				List<IPackagePattern> patterns = info.getPatterns();
				for(int i = 0; i < patterns.size() && i < 9; ++i) {
					stacks.set(90+i, patterns.get(i).getOutput().copy());
				}
				if(mark) {
					syncTile();
					markDirty();
				}
			}
		}
		else if(recipeInfo != null) {
			recipeInfo = null;
			if(!recipeType.canSetOutput()) {
				for(int i = 81; i < 90; ++i) {
					stacks.set(i, null);
				}
			}
			for(int i = 90; i < 99; ++i) {
				stacks.set(i, null);
			}
			if(mark) {
				syncTile();
				markDirty();
			}
		}
	}

	public void cycleRecipeType(boolean reverse) {
		validateRecipeType();
		recipeType = ApiImpl.INSTANCE.getNextRecipeType(recipeType, reverse);
		validateRecipeType();
		Set<Integer> enabledSlots = recipeType.getEnabledSlots();
		for(int i = 0; i < 90; ++i) {
			if(!enabledSlots.contains(i)) {
				stacks.set(i, null);
			}
		}
		updateRecipeInfo(true);
		for(InventoryEncoderPattern patternInventory : tile.patternInventories) {
			if(patternInventory != this) {
				patternInventory.setRecipeTypeIfEmpty(recipeType);
			}
		}
	}

	public void setRecipeTypeIfEmpty(IPackageRecipeType recipeType) {
		if(stacks.stream().allMatch(Objects::isNull)) {
			this.recipeType = recipeType;
			validateRecipeType();
			updateRecipeInfo(true);
		}
	}

	public void setRecipe(Map<Integer, ItemStack> map) {
		if(recipeType.canSetOutput()) {
			for(int i = 0; i < 90; ++i) {
				stacks.set(i, null);
			}
		}
		else {
			for(int i = 0; i < 81; ++i) {
				stacks.set(i, null);
			}
		}
		if(map != null) {
			for(Map.Entry<Integer, ItemStack> entry : map.entrySet()) {
				stacks.set(entry.getKey(), entry.getValue());
			}
		}
		updateRecipeInfo(true);
	}
}
