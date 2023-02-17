package thelm.packagedauto.util;

import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.api.PackagedAutoApi;

public class ApiImpl extends PackagedAutoApi {

	public static final ApiImpl INSTANCE = new ApiImpl();

	private static final NavigableMap<String, IPackageRecipeType> REGISTRY = new TreeMap<>();
	private static final BiMap<Integer, IPackageRecipeType> IDS = HashBiMap.create(4);
	private static int id = 0;

	private ApiImpl() {}

	@Override
	public boolean registerRecipeType(IPackageRecipeType type) {
		if(REGISTRY.containsKey(type.getName())) {
			return false;
		}
		REGISTRY.put(type.getName(), type);
		IDS.put(id++, type);
		return true;
	}

	@Override
	public IPackageRecipeType getRecipeType(String name) {
		return REGISTRY.get(name);
	}

	@Override
	public IPackageRecipeType getRecipeType(int id) {
		return IDS.get(id);
	}

	@Override
	public int getId(IPackageRecipeType type) {
		return IDS.inverse().get(type);
	}

	@Override
	public NavigableMap<String, IPackageRecipeType> getRecipeTypeRegistry() {
		return Collections.unmodifiableNavigableMap(REGISTRY);
	}

	@Override
	public IPackageRecipeType getNextRecipeType(IPackageRecipeType type, boolean reverse) {
		int toGet = Math.floorMod(getId(type) + (!reverse ? 1 : -1), REGISTRY.size());
		IPackageRecipeType ret = getRecipeType(toGet);
		return ret;
	}
}
