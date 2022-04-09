package thelm.packagedauto.util;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableSortedMap;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import thelm.packagedauto.api.IMiscHelper;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.api.IVolumeType;
import thelm.packagedauto.api.PackagedAutoApi;

public class ApiImpl extends PackagedAutoApi {

	public static final ApiImpl INSTANCE = new ApiImpl();

	private static final Logger LOGGER = LogManager.getLogger();
	private static final SortedMap<ResourceLocation, IPackageRecipeType> RECIPE_REGISTRY = new TreeMap<>();
	private static final CrudeIncrementalIntIdentityHashBiMap<IPackageRecipeType> RECIPE_ID_MAP = CrudeIncrementalIntIdentityHashBiMap.create(4);
	private static int id = 0;
	private static final Map<ResourceLocation, IVolumeType> VOLUME_REGISTRY = new TreeMap<>();
	private static final Map<Class<?>, IVolumeType> VOLUME_CLASS_REGISTRY = new HashMap<>();

	private ApiImpl() {}

	@Override
	public boolean registerRecipeType(IPackageRecipeType type) {
		if(RECIPE_REGISTRY.containsKey(type.getName())) {
			return false;
		}
		RECIPE_REGISTRY.put(type.getName(), type);
		RECIPE_ID_MAP.addMapping(type, id++);
		return true;
	}

	@Override
	public IPackageRecipeType getRecipeType(ResourceLocation name) {
		return RECIPE_REGISTRY.get(name);
	}

	@Override
	public IPackageRecipeType getRecipeType(int id) {
		return RECIPE_ID_MAP.byId(id);
	}

	@Override
	public int getId(IPackageRecipeType type) {
		return RECIPE_ID_MAP.getId(type);
	}

	@Override
	public NavigableMap<ResourceLocation, IPackageRecipeType> getRecipeTypeRegistry() {
		return ImmutableSortedMap.copyOf(RECIPE_REGISTRY);
	}

	@Override
	public IPackageRecipeType getNextRecipeType(IPackageRecipeType type, boolean reverse) {
		int toGet = RECIPE_ID_MAP.getId(type) + (!reverse ? 1 : -1);
		IPackageRecipeType ret = RECIPE_ID_MAP.byId(toGet);
		if(ret == null) {
			ret = RECIPE_ID_MAP.byId(!reverse ? 0 : RECIPE_ID_MAP.size()-1);
		}
		return ret;
	}

	@Override
	public boolean registerVolumeType(IVolumeType type) {
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
	public IMiscHelper miscHelper() {
		return MiscHelper.INSTANCE;
	}
}
