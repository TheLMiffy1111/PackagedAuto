package thelm.packagedauto.api;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface IPackageRecipeType {

	ResourceLocation getName();

	MutableComponent getDisplayName();

	MutableComponent getShortDisplayName();

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

	default boolean hasCraftingRemainingItem() {
		return true;
	}

	default List<ResourceLocation> getJEICategories() {
		return new ArrayList<>();
	}

	default Int2ObjectMap<ItemStack> getRecipeTransferMap(IRecipeSlotsViewWrapper recipeLayoutWrapper) {
		return new Int2ObjectOpenHashMap<>();
	}

	Object getRepresentation();

	Vec3i getSlotColor(int slot);
}
