package thelm.packagedauto.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeList;

public class PackageRecipeList implements IPackageRecipeList {

	private List<IPackageRecipeInfo> recipeList = new ArrayList<>();

	public PackageRecipeList(CompoundTag nbt) {
		load(nbt);
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
	public void load(CompoundTag nbt) {
		recipeList.clear();
		if(nbt != null) {
			recipeList.addAll(MiscHelper.INSTANCE.loadRecipeList(nbt.getList("Recipes", 10)));
		}
	}

	@Override
	public void save(CompoundTag nbt) {
		ListTag tagList = MiscHelper.INSTANCE.saveRecipeList(new ListTag(), recipeList);
		if(!tagList.isEmpty()) {
			nbt.put("Recipes", tagList);
		}
	}
}
