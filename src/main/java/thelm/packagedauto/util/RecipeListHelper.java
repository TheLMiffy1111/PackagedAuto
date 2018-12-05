package thelm.packagedauto.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.IRecipeList;
import thelm.packagedauto.api.IRecipeType;
import thelm.packagedauto.api.RecipeTypeRegistry;

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
				IRecipeType recipeType = RecipeTypeRegistry.getRecipeType(new ResourceLocation(tag.getString("RecipeType")));
				if(recipeType == null) {
					continue;
				}
				IRecipeInfo recipe = recipeType.getNewRecipeInfo();
				recipe.readFromNBT(tag);
				if(recipe.isValid()) {
					recipeList.add(recipe);
				}
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		NBTTagList tagList = new NBTTagList();
		for(IRecipeInfo recipe : recipeList) {
			NBTTagCompound tag = recipe.writeToNBT(new NBTTagCompound());
			tag.setString("RecipeType", recipe.getRecipeType().getName().toString());
			tagList.appendTag(tag);
		}
		nbt.setTag("Recipes", tagList);
		return nbt;
	}
}
