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
			recipeList.addAll(MiscUtil.readRecipeListFromNBT(nbt.getTagList("Recipes", 10)));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		NBTTagList tagList = MiscUtil.writeRecipeListToNBT(new NBTTagList(), recipeList);
		if(!tagList.isEmpty()) {
			nbt.setTag("Recipes", tagList);
		}
		return nbt;
	}
}
