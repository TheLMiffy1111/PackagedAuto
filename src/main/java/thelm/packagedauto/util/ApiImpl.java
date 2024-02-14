package thelm.packagedauto.util;

import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.ResourceLocation;
import thelm.packagedauto.api.IMiscHelper;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.api.PackagedAutoApi;

public class ApiImpl extends PackagedAutoApi {

	public static final ApiImpl INSTANCE = new ApiImpl();

	private static final Logger LOGGER = LogManager.getLogger();
	private static final NavigableMap<ResourceLocation, IPackageRecipeType> REGISTRY = new TreeMap<>();
	private static final Multimap<String, IPackageRecipeType> ORDER = MultimapBuilder.treeKeys().arrayListValues().build();
	private static final IntIdentityHashBiMap<IPackageRecipeType> IDS = new IntIdentityHashBiMap<>(4);

	private ApiImpl() {}

	@Override
	public synchronized boolean registerRecipeType(IPackageRecipeType type) {
		if(REGISTRY.containsKey(type.getName())) {
			return false;
		}
		IDS.clear();
		REGISTRY.put(type.getName(), type);
		ORDER.put(type.getName().getNamespace(), type);
		return true;
	}

	@Override
	public IPackageRecipeType getRecipeType(ResourceLocation name) {
		return REGISTRY.get(name);
	}

	@Override
	public IPackageRecipeType getRecipeType(int id) {
		computeIds();
		return IDS.byId(id);
	}

	@Override
	public int getId(IPackageRecipeType type) {
		computeIds();
		return IDS.getId(type);
	}

	private void computeIds() {
		if(IDS.size() == 0) {
			ORDER.forEach((mod, type)->IDS.add(type));
		}
	}

	@Override
	public NavigableMap<ResourceLocation, IPackageRecipeType> getRecipeTypeRegistry() {
		return Collections.unmodifiableNavigableMap(REGISTRY);
	}

	@Override
	public IPackageRecipeType getNextRecipeType(IPackageRecipeType type, boolean reverse) {
		int toGet = Math.floorMod(getId(type) + (!reverse ? 1 : -1), REGISTRY.size());
		IPackageRecipeType ret = getRecipeType(toGet);
		return ret;
	}

	@Override
	public IMiscHelper miscHelper() {
		return MiscHelper.INSTANCE;
	}
}
