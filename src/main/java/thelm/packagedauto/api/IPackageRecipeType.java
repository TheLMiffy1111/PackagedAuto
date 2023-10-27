package thelm.packagedauto.api;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.IFormattableTextComponent;

public interface IPackageRecipeType {

	ResourceLocation getName();

	IFormattableTextComponent getDisplayName();

	IFormattableTextComponent getShortDisplayName();

	IPackageRecipeInfo getNewRecipeInfo();

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

	default List<ResourceLocation> getJEICategories() {
		return new ArrayList<>();
	}

	default Int2ObjectMap<ItemStack> getRecipeTransferMap(IRecipeLayoutWrapper recipeLayoutWrapper) {
		return new Int2ObjectOpenHashMap<>();
	}

	Object getRepresentation();

	Vector3i getSlotColor(int slot);
}
