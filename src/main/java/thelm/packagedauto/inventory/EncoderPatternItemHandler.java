package thelm.packagedauto.inventory;

import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.block.entity.EncoderBlockEntity;
import thelm.packagedauto.recipe.ProcessingPackageRecipeType;
import thelm.packagedauto.util.ApiImpl;

public class EncoderPatternItemHandler extends BaseItemHandler {

	public final EncoderBlockEntity blockEntity;
	public IPackageRecipeType recipeType;
	public IPackageRecipeInfo recipeInfo;

	public EncoderPatternItemHandler(EncoderBlockEntity blockEntity) {
		super(blockEntity, 99);
		this.blockEntity = blockEntity;
		validateRecipeType();
	}

	@Override
	public void onContentsChanged(int slot) {
		//maybe add check to see where this is called
		updateRecipeInfo();
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		recipeType = ApiImpl.INSTANCE.getRecipeType(new ResourceLocation(nbt.getString("RecipeType")));
		validateRecipeType();
		updateRecipeInfo();
	}

	@Override
	public void save(CompoundTag nbt) {
		super.save(nbt);
		validateRecipeType();
		nbt.putString("RecipeType", recipeType.getName().toString());
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
		info.generateFromStacks(stacks.subList(0, 81), recipeType.canSetOutput() ? stacks.subList(81, 90) : List.of(), blockEntity.getLevel());
		if(info.isValid()) {
			if(recipeInfo == null || !recipeInfo.equals(info)) {
				recipeInfo = info;
				if(!recipeType.canSetOutput()) {
					for(int i = 81; i < 90; ++i) {
						stacks.set(i, ItemStack.EMPTY);
					}
					List<ItemStack> outputs = info.getOutputs();
					int size = outputs.size();
					for(int i = 0; i < size; ++i) {
						stacks.set(81+i, outputs.get(i).copy());
					}
				}
				for(int i = 90; i < 99; ++i) {
					stacks.set(i, ItemStack.EMPTY);
				}
				List<IPackagePattern> patterns = info.getPatterns();
				for(int i = 0; i < patterns.size() && i < 9; ++i) {
					stacks.set(90+i, patterns.get(i).getOutput().copy());
				}
				sync(false);
				setChanged();
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
			sync(false);
			setChanged();
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
		for(EncoderPatternItemHandler patternItemHandler : blockEntity.patternItemHandlers) {
			if(patternItemHandler != this) {
				patternItemHandler.setRecipeTypeIfEmpty(recipeType);
			}
		}
	}

	public void setRecipeTypeIfEmpty(IPackageRecipeType recipeType) {
		if(stacks.stream().allMatch(ItemStack::isEmpty)) {
			this.recipeType = recipeType;
			validateRecipeType();
			updateRecipeInfo();
		}
	}

	public void setRecipe(Int2ObjectMap<ItemStack> map) {
		if(recipeType.canSetOutput()) {
			for(int i = 0; i < 90; ++i) {
				stacks.set(i, ItemStack.EMPTY);
			}
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
