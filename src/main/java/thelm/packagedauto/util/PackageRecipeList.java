package thelm.packagedauto.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeList;

public class PackageRecipeList implements IPackageRecipeList {

	private List<IPackageRecipeInfo> recipeList = new ArrayList<>();

	public PackageRecipeList(World world, CompoundNBT nbt) {
		read(world, nbt);
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
	public void read(World world, CompoundNBT nbt) {
		recipeList.clear();
		if(nbt != null) {
			ListNBT tagList = nbt.getList("Recipes", 10);
			for(int i = 0; i < tagList.size(); ++i) {
				CompoundNBT tag = tagList.getCompound(i);
				IPackageRecipeInfo recipe = MiscHelper.INSTANCE.readRecipe(tag);
				if(recipe != null) {
					recipeList.add(recipe);
				}
			}
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		ListNBT tagList = new ListNBT();
		for(IPackageRecipeInfo recipe : recipeList) {
			CompoundNBT tag = MiscHelper.INSTANCE.writeRecipe(new CompoundNBT(), recipe);
			tagList.add(tag);
		}
		nbt.put("Recipes", tagList);
		return nbt;
	}
}
