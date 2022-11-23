package thelm.packagedauto.inventory;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
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
		try {
			info.generateFromStacks(stacks.subList(0, 81), recipeType.canSetOutput() ? stacks.subList(81, 90) : Collections.emptyList(), tile.getWorld());
		}
		catch(AbstractMethodError error) {
			try {
				Method oldGenerateFromStacksMethod = info.getClass().getMethod("generateFromStacks", List.class, List.class);
				oldGenerateFromStacksMethod.invoke(info, stacks.subList(0, 81), recipeType.canSetOutput() ? stacks.subList(81, 90) : Collections.emptyList());
			}
			catch(Exception exception) {
				exception.addSuppressed(error);
				exception.printStackTrace();
			}
		}
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
		recipeType = RecipeTypeRegistry.getNextRecipeType(recipeType, reverse);
		validateRecipeType();
		IntSet enabledSlots = recipeType.getEnabledSlots();
		for(int i = 0; i < 90; ++i) {
			if(!enabledSlots.contains(i)) {
				stacks.set(i, ItemStack.EMPTY);
			}
		}
		updateRecipeInfo();
		for(InventoryEncoderPattern patternInventory : tile.patternInventories) {
			if(patternInventory != this) {
				patternInventory.setRecipeTypeIfEmpty(recipeType);
			}
		}
	}

	public void setRecipeTypeIfEmpty(IRecipeType recipeType) {
		if(stacks.stream().allMatch(ItemStack::isEmpty)) {
			this.recipeType = recipeType;
			validateRecipeType();
			updateRecipeInfo();
		}
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
