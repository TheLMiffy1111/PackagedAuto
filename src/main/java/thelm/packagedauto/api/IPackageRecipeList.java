package thelm.packagedauto.api;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public interface IPackageRecipeList {

	void load(Level level, CompoundTag nbt);

	void save(CompoundTag nbt);

	List<IPackageRecipeInfo> getRecipeList();

	void setRecipeList(List<IPackageRecipeInfo> recipeList);
}
