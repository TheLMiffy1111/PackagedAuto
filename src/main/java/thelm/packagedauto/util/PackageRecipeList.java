package thelm.packagedauto.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeList;

public class PackageRecipeList implements IPackageRecipeList {

	private List<IPackageRecipeInfo> recipeList = new ArrayList<>();

	public PackageRecipeList(CompoundNBT nbt) {
		read(nbt);
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
	public void read(CompoundNBT nbt) {
		recipeList.clear();
		if(nbt != null) {
			recipeList.addAll(MiscHelper.INSTANCE.readRecipeList(nbt.getList("Recipes", 10)));
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		ListNBT tagList = MiscHelper.INSTANCE.writeRecipeList(new ListNBT(), recipeList);
		if(!tagList.isEmpty()) {
			nbt.put("Recipes", tagList);
		}
		return nbt;
	}
}
