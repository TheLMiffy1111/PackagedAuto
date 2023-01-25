package thelm.packagedauto.api;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

public interface IPackageRecipeList {

	void readFromNBT(NBTTagCompound nbt);

	NBTTagCompound writeToNBT(NBTTagCompound nbt);

	List<IPackageRecipeInfo> getRecipeList();

	void setRecipeList(List<IPackageRecipeInfo> recipeList);
}
