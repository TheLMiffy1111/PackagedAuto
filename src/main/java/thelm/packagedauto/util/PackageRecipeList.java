package thelm.packagedauto.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeList;

public class PackageRecipeList implements IPackageRecipeList {

	private List<IPackageRecipeInfo> recipeList = new ArrayList<>();

	public PackageRecipeList(Level level, CompoundTag nbt) {
		load(level, nbt);
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
	public void load(Level level, CompoundTag nbt) {
		recipeList.clear();
		if(nbt != null) {
			ListTag tagList = nbt.getList("Recipes", 10);
			for(int i = 0; i < tagList.size(); ++i) {
				CompoundTag tag = tagList.getCompound(i);
				IPackageRecipeInfo recipe = MiscHelper.INSTANCE.loadRecipe(tag);
				if(recipe != null) {
					recipeList.add(recipe);
				}
			}
		}
	}

	@Override
	public void save(CompoundTag nbt) {
		ListTag tagList = new ListTag();
		for(IPackageRecipeInfo recipe : recipeList) {
			CompoundTag tag = MiscHelper.INSTANCE.saveRecipe(new CompoundTag(), recipe);
			tagList.add(tag);
		}
		nbt.put("Recipes", tagList);
	}
}
