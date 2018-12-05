package thelm.packagedauto.inventory;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.IRecipeType;
import thelm.packagedauto.api.RecipeTypeRegistry;
import thelm.packagedauto.recipe.RecipeTypeProcessing;
import thelm.packagedauto.tile.TileEncoder;

public class InventoryEncoderPattern extends InventoryTileBase {

	public final TileEncoder tile;
	public IRecipeType recipeType;
	public IRecipeInfo recipeInfo;

	public InventoryEncoderPattern(TileEncoder tile) {
		super(tile, 99);
		this.tile = tile;
		validateRecipeType();
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		super.setInventorySlotContents(index, stack);
		//maybe add check to see where this is called
		updateRecipeInfo();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		recipeType = RecipeTypeRegistry.getRecipeType(new ResourceLocation(nbt.getString("RecipeType")));
		validateRecipeType();
		updateRecipeInfo();
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
			recipeType = RecipeTypeProcessing.INSTANCE;
		}
	}

	public void updateRecipeInfo() {
		validateRecipeType();
		IRecipeInfo info = recipeType.getNewRecipeInfo();
		info.generateFromStacks(stacks.subList(0, 81), recipeType.canSetOutput() ? stacks.subList(81, 90) : Collections.emptyList());
		if(info.isValid()) {
			recipeInfo = info;
			if(!recipeType.canSetOutput()) {
				List<ItemStack> stacks = info.getOutputs();
				int size = stacks.size();
				int startIndex = 81;
				switch(size) {
				case 1: startIndex += 1; 
				case 2:
				case 3: startIndex += 3;
				}
				for(int i = 0; i < size; ++i) {
					this.stacks.set(startIndex+i, stacks.get(i).copy());
				}
			}
			List<IPackagePattern> patterns = info.getPatterns();
			for(int i = 0; i < patterns.size() && i < 9; ++i) {
				this.stacks.set(90+i, patterns.get(i).getOutput().copy());
			}
		}
		else {
			recipeInfo = null;
			if(!recipeType.canSetOutput()) {
				for(int i = 81; i < 90; ++i) {
					stacks.set(i, ItemStack.EMPTY);
				}
			}
			for(int i = 90; i < 99; ++i) {
				stacks.set(i, ItemStack.EMPTY);
			}
		}
		syncTile(false);
		markDirty();
	}

	public void cycleRecipeType() {
		validateRecipeType();
		NavigableMap<ResourceLocation, IRecipeType> registry = RecipeTypeRegistry.getRegistry();
		Entry<ResourceLocation, IRecipeType> entry = registry.higherEntry(recipeType.getName());
		if(entry == null) {
			entry = registry.firstEntry();
		}
		recipeType = entry.getValue();
		updateRecipeInfo();
	}

	public void setRecipe(Int2ObjectMap<ItemStack> map) {
		stacks.clear();
		for(Int2ObjectMap.Entry<ItemStack> entry : map.int2ObjectEntrySet()) {
			stacks.set(entry.getIntKey(), entry.getValue());
		}
		updateRecipeInfo();
	}
}
