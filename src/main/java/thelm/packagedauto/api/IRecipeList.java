package thelm.packagedauto.api;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

public interface IRecipeList {

	void readFromNBT(NBTTagCompound nbt);

	NBTTagCompound writeToNBT(NBTTagCompound nbt);

	List<IRecipeInfo> getRecipeList();

	void setRecipeList(List<IRecipeInfo> recipeList);
}
