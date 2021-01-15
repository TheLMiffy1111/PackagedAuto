package thelm.packagedauto.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.IRecipeList;
import thelm.packagedauto.api.MiscUtil;

public class RecipeListHelper implements IRecipeList {

	private List<IRecipeInfo> recipeList = new ArrayList<>();

	public RecipeListHelper(NBTTagCompound nbt) {
		readFromNBT(nbt);
	}

	public RecipeListHelper(List<IRecipeInfo> recipeList) {
		setRecipeList(recipeList);
	}

	@Override
	public List<IRecipeInfo> getRecipeList() {
		return Collections.unmodifiableList(recipeList);
	}

	@Override
	public void setRecipeList(List<IRecipeInfo> recipeList) {
		this.recipeList.clear();
		this.recipeList.addAll(recipeList);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		recipeList.clear();
		if(nbt != null) {
			NBTTagList tagList = nbt.getTagList("Recipes", 10);
			for(int i = 0; i < tagList.tagCount(); ++i) {
				NBTTagCompound tag = tagList.getCompoundTagAt(i);
				IRecipeInfo recipe = MiscUtil.readRecipeFromNBT(tag);
				if(recipe != null) {
					recipeList.add(recipe);
				}
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		NBTTagList tagList = new NBTTagList();
		for(IRecipeInfo recipe : recipeList) {
			NBTTagCompound tag = MiscUtil.writeRecipeToNBT(new NBTTagCompound(), recipe);
			tagList.appendTag(tag);
		}
		nbt.setTag("Recipes", tagList);
		return nbt;
	}
}
