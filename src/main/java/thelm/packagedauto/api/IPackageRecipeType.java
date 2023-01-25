package thelm.packagedauto.api;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import codechicken.nei.recipe.IRecipeHandler;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;

public interface IPackageRecipeType {

	String getName();

	String getLocalizedName();

	String getLocalizedNameShort();

	IPackageRecipeInfo getNewRecipeInfo();

	Set<Integer> getEnabledSlots();

	boolean canSetOutput();

	boolean hasMachine();

	default List<String> getNEICategories() {
		return new ArrayList<>();
	}

	@Optional.Method(modid="NotEnoughItems")
	default Map<Integer, ItemStack> getRecipeTransferMap(IRecipeHandler recipeHandler, int recipeIndex, Set<String> categories) {
		return new HashMap<>();
	}

	@SideOnly(Side.CLIENT)
	Object getRepresentation();

	@SideOnly(Side.CLIENT)
	Color getSlotColor(int slot);
}
