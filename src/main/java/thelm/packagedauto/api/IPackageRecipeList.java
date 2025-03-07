package thelm.packagedauto.api;

import java.util.List;

import net.minecraft.nbt.CompoundNBT;

public interface IPackageRecipeList {

	void read(CompoundNBT nbt);

	CompoundNBT write(CompoundNBT nbt);

	List<IPackageRecipeInfo> getRecipeList();

	void setRecipeList(List<IPackageRecipeInfo> recipeList);
}
