package thelm.packagedauto.api;

import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.collect.ImmutableSortedMap;

import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.ResourceLocation;

public class RecipeTypeRegistry {

	private RecipeTypeRegistry() {}

	private static final SortedMap<ResourceLocation, IRecipeType> REGISTRY = new TreeMap<>();
	private static final IntIdentityHashBiMap<IRecipeType> ID_MAP = new IntIdentityHashBiMap<>(4);
	private static int id = 0;

	public static boolean registerRecipeType(IRecipeType type) {
		if(REGISTRY.containsKey(type.getName())) {
			return false;
		}
		REGISTRY.put(type.getName(), type);
		ID_MAP.put(type, id++);
		return true;
	}

	public static IRecipeType getRecipeType(ResourceLocation name) {
		return REGISTRY.get(name);
	}

	public static IRecipeType getRecipeType(int id) {
		return ID_MAP.get(id);
	}

	public static int getId(IRecipeType type) {
		return ID_MAP.getId(type);
	}

	public static NavigableMap<ResourceLocation, IRecipeType> getRegistry() {
		return ImmutableSortedMap.copyOf(REGISTRY);
	}

	public static IRecipeType getNextRecipeType(IRecipeType type, boolean reverse) {
		int toGet = ID_MAP.getId(type) + (!reverse ? 1 : -1);
		IRecipeType ret = ID_MAP.get(toGet);
		if(ret == null) {
			ret = ID_MAP.get(!reverse ? 0 : ID_MAP.size()-1);
		}
		return ret;
	}
}
