package thelm.packagedauto.api;

import java.util.NavigableMap;

import net.minecraft.resources.ResourceLocation;

public abstract class PackagedAutoApi {

	private static PackagedAutoApi instance;

	public static PackagedAutoApi instance() {
		if(instance == null) {
			try {
				instance = PackagedAutoApi.class.cast(
						Class.forName("thelm.packagedauto.util.ApiImpl").
						getField("INSTANCE").
						get(null));
			}
			catch(Exception e) {
				throw new IllegalStateException("Unable to obtain api implementation.", e);
			}
		}
		return instance;
	}

	public abstract boolean registerRecipeType(IPackageRecipeType type);

	public abstract IPackageRecipeType getRecipeType(ResourceLocation name);

	public abstract IPackageRecipeType getRecipeType(int id);

	public abstract int getId(IPackageRecipeType type);

	public abstract NavigableMap<ResourceLocation, IPackageRecipeType> getRecipeTypeRegistry();

	public abstract IPackageRecipeType getNextRecipeType(IPackageRecipeType type, boolean reverse);

	public abstract boolean registerVolumeType(IVolumeType type);

	public abstract IVolumeType getVolumeType(ResourceLocation name);

	public abstract IVolumeType getVolumeType(Class<?> typeClass);

	public abstract IMiscHelper miscHelper();
}
