package thelm.packagedauto.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import thelm.packagedauto.api.IMiscHelper;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.api.IVolumeType;
import thelm.packagedauto.api.PackagedAutoApi;

public class ApiImpl extends PackagedAutoApi {

	public static final ApiImpl INSTANCE = new ApiImpl();

	private static final Logger LOGGER = LogManager.getLogger();
	private static final NavigableMap<ResourceLocation, IPackageRecipeType> RECIPE_REGISTRY = new TreeMap<>();
	private static final Multimap<String, IPackageRecipeType> RECIPE_ORDER = MultimapBuilder.treeKeys().arrayListValues().build();
	private static final CrudeIncrementalIntIdentityHashBiMap<IPackageRecipeType> RECIPE_IDS = CrudeIncrementalIntIdentityHashBiMap.create(4);
	private static final NavigableMap<ResourceLocation, IVolumeType> VOLUME_REGISTRY = new TreeMap<>();
	private static final Map<Class<?>, IVolumeType> VOLUME_CLASS_REGISTRY = new HashMap<>();

	private ApiImpl() {}

	@Override
	public synchronized boolean registerRecipeType(IPackageRecipeType type) {
		if(RECIPE_REGISTRY.containsKey(type.getName())) {
			return false;
		}
		RECIPE_IDS.clear();
		RECIPE_REGISTRY.put(type.getName(), type);
		RECIPE_ORDER.put(type.getName().getNamespace(), type);
		return true;
	}

	@Override
	public IPackageRecipeType getRecipeType(ResourceLocation name) {
		return RECIPE_REGISTRY.get(name);
	}

	@Override
	public IPackageRecipeType getRecipeType(int id) {
		computeIds();
		return RECIPE_IDS.byId(id);
	}

	@Override
	public int getId(IPackageRecipeType type) {
		computeIds();
		return RECIPE_IDS.getId(type);
	}

	private synchronized void computeIds() {
		if(RECIPE_IDS.size() == 0) {
			RECIPE_ORDER.forEach((mod, type)->RECIPE_IDS.add(type));
		}
	}

	@Override
	public NavigableMap<ResourceLocation, IPackageRecipeType> getRecipeTypeRegistry() {
		return Collections.unmodifiableNavigableMap(RECIPE_REGISTRY);
	}

	@Override
	public IPackageRecipeType getNextRecipeType(IPackageRecipeType type, boolean reverse) {
		int toGet = Math.floorMod(getId(type) + (!reverse ? 1 : -1), RECIPE_REGISTRY.size());
		IPackageRecipeType ret = getRecipeType(toGet);
		return ret;
	}

	@Override
	public synchronized boolean registerVolumeType(IVolumeType type) {
		if(VOLUME_REGISTRY.containsKey(type.getName()) || VOLUME_CLASS_REGISTRY.containsKey(type.getTypeClass())) {
			return false;
		}
		VOLUME_REGISTRY.put(type.getName(), type);
		VOLUME_CLASS_REGISTRY.put(type.getTypeClass(), type);
		return true;
	}

	@Override
	public IVolumeType getVolumeType(ResourceLocation name) {
		return VOLUME_REGISTRY.get(name);
	}

	@Override
	public IVolumeType getVolumeType(Class<?> typeClass) {
		return VOLUME_CLASS_REGISTRY.get(typeClass);
	}

	@Override
	public NavigableMap<ResourceLocation, IVolumeType> getVolumeTypeRegistry() {
		return Collections.unmodifiableNavigableMap(VOLUME_REGISTRY);
	}

	@Override
	public IMiscHelper miscHelper() {
		return MiscHelper.INSTANCE;
	}
}
