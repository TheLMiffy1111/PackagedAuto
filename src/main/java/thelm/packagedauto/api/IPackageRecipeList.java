package thelm.packagedauto.api;

import java.util.List;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

public interface IPackageRecipeList {

	void read(World world, CompoundNBT nbt);

	CompoundNBT write(CompoundNBT nbt);

	List<IPackageRecipeInfo> getRecipeList();

	void setRecipeList(List<IPackageRecipeInfo> recipeList);
}
