package thelm.packagedauto.util;

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
import thelm.packagedauto.api.PackagedAutoApi;

public class ApiImpl extends PackagedAutoApi {

	public static final ApiImpl INSTANCE = new ApiImpl();

	private static final Logger LOGGER = LogManager.getLogger();
	private static final SortedMap<ResourceLocation, IPackageRecipeType> REGISTRY = new TreeMap<>();
	private static final CrudeIncrementalIntIdentityHashBiMap<IPackageRecipeType> ID_MAP = CrudeIncrementalIntIdentityHashBiMap.create(4);
	private static int id = 0;

	private ApiImpl() {}

	@Override
	public boolean registerRecipeType(IPackageRecipeType type) {
		if(REGISTRY.containsKey(type.getName())) {
			return false;
		}
		REGISTRY.put(type.getName(), type);
		ID_MAP.addMapping(type, id++);
		return true;
	}

	@Override
	public IPackageRecipeType getRecipeType(ResourceLocation name) {
		return REGISTRY.get(name);
	}

	@Override
	public IPackageRecipeType getRecipeType(int id) {
		return ID_MAP.byId(id);
	}

	@Override
	public int getId(IPackageRecipeType type) {
		return ID_MAP.getId(type);
	}

	@Override
	public NavigableMap<ResourceLocation, IPackageRecipeType> getRecipeTypeRegistry() {
		return ImmutableSortedMap.copyOf(REGISTRY);
	}

	@Override
	public IPackageRecipeType getNextRecipeType(IPackageRecipeType type, boolean reverse) {
		int toGet = ID_MAP.getId(type) + (!reverse ? 1 : -1);
		IPackageRecipeType ret = ID_MAP.byId(toGet);
		if(ret == null) {
			ret = ID_MAP.byId(!reverse ? 0 : ID_MAP.size()-1);
		}
		return ret;
	}

	@Override
	public IMiscHelper miscHelper() {
		return MiscHelper.INSTANCE;
	}
}
