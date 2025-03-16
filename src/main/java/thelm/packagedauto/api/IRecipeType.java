package thelm.packagedauto.api;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IRecipeType {

	ResourceLocation getName();

	String getLocalizedName();

	String getLocalizedNameShort();

	IRecipeInfo getNewRecipeInfo();

	IntSet getEnabledSlots();

	default boolean canSetOutput() {
		return false;
	}

	default boolean hasMachine() {
		return true;
	}

	default boolean isOrdered() {
		return false;
	}

	default List<String> getJEICategories() {
		return Collections.emptyList();
	}

	@Optional.Method(modid="jei")
	default Int2ObjectMap<ItemStack> getRecipeTransferMap(IRecipeWrapper recipeWrapper, IRecipeLayout recipeLayout, String category) {
		return getRecipeTransferMap(recipeLayout, category);
	}

	@Optional.Method(modid="jei")
	default Int2ObjectMap<ItemStack> getRecipeTransferMap(IRecipeLayout recipeLayout, String category) {
		return Int2ObjectMaps.emptyMap();
	}

	@SideOnly(Side.CLIENT)
	Object getRepresentation();

	@SideOnly(Side.CLIENT)
	Color getSlotColor(int slot);
}
