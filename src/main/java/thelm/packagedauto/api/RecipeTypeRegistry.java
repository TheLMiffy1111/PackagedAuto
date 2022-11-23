package thelm.packagedauto.api;

import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;

import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.ResourceLocation;

public class RecipeTypeRegistry {

	private RecipeTypeRegistry() {}

	private static final NavigableMap<ResourceLocation, IRecipeType> REGISTRY = new TreeMap<>();
	private static final IntIdentityHashBiMap<IRecipeType> IDS = new IntIdentityHashBiMap<>(4);
	private static int id = 0;

	public static boolean registerRecipeType(IRecipeType type) {
		if(REGISTRY.containsKey(type.getName())) {
			return false;
		}
		REGISTRY.put(type.getName(), type);
		IDS.add(type);
		return true;
	}

	public static IRecipeType getRecipeType(ResourceLocation name) {
		return REGISTRY.get(name);
	}

	public static IRecipeType getRecipeType(int id) {
		return IDS.get(id);
	}

	public static int getId(IRecipeType type) {
		return IDS.getId(type);
	}

	public static NavigableMap<ResourceLocation, IRecipeType> getRegistry() {
		return Collections.unmodifiableNavigableMap(REGISTRY);
	}

	public static IRecipeType getNextRecipeType(IRecipeType type, boolean reverse) {
		int toGet = Math.floorMod(getId(type) + (!reverse ? 1 : -1), REGISTRY.size());
		IRecipeType ret = getRecipeType(toGet);
		return ret;
	}
}
