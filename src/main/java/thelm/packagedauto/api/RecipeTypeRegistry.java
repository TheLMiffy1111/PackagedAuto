package thelm.packagedauto.api;

import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;

import net.minecraft.util.ResourceLocation;

public class RecipeTypeRegistry {

	private RecipeTypeRegistry() {}

	private static final SortedMap<ResourceLocation, IRecipeType> REGISTRY = new TreeMap<>();

	public static boolean registerRecipeType(IRecipeType type) {
		if(REGISTRY.containsKey(type.getName())) {
			return false;
		}
		REGISTRY.put(type.getName(), type);
		return true;
	}

	public static IRecipeType getRecipeType(ResourceLocation name) {
		return REGISTRY.get(name);
	}

	public static NavigableMap<ResourceLocation, IRecipeType> getRegistry() {
		return ImmutableSortedMap.copyOf(REGISTRY);
	}
}
