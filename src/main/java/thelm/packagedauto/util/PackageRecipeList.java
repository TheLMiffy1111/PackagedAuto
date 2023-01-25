package thelm.packagedauto.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeList;

public class PackageRecipeList implements IPackageRecipeList {

	private List<IPackageRecipeInfo> recipeList = new ArrayList<>();

	public PackageRecipeList(NBTTagCompound nbt) {
		readFromNBT(nbt);
	}

	public PackageRecipeList(List<IPackageRecipeInfo> recipeList) {
		setRecipeList(recipeList);
	}

	@Override
	public List<IPackageRecipeInfo> getRecipeList() {
		return Collections.unmodifiableList(recipeList);
	}

	@Override
	public void setRecipeList(List<IPackageRecipeInfo> recipeList) {
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
				IPackageRecipeInfo recipe = MiscHelper.INSTANCE.readRecipeFromNBT(tag);
				if(recipe != null) {
					recipeList.add(recipe);
				}
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		NBTTagList tagList = new NBTTagList();
		for(IPackageRecipeInfo recipe : recipeList) {
			NBTTagCompound tag = MiscHelper.INSTANCE.writeRecipeToNBT(new NBTTagCompound(), recipe);
			tagList.appendTag(tag);
		}
		nbt.setTag("Recipes", tagList);
		return nbt;
	}
}
