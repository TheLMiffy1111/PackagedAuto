package thelm.packagedauto.proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import cpw.mods.fml.common.versioning.DefaultArtifactVersion;
import cpw.mods.fml.common.versioning.InvalidVersionSpecificationException;
import cpw.mods.fml.common.versioning.VersionRange;
import thelm.packagedauto.integration.nei.NEIHandler;
import thelm.packagedauto.util.MiscHelper;

public class ClientProxy extends CommonProxy {

	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	protected void registerNEI() {
		MiscHelper.INSTANCE.conditionalSupplier(this::isNEIRequiredVersion, ()->()->{
			NEIHandler.INSTANCE.register();
			neiLoaded = true;
			return null;
		}, ()->()->null).get();
	}

	protected boolean isNEIRequiredVersion() {
		if(!Loader.isModLoaded("NotEnoughItems")) {
			return false;
		}
		VersionRange versionRange;
		try {
			versionRange = VersionRange.createFromVersionSpec("[2.3.7,)");
		}
		catch(InvalidVersionSpecificationException e) {
			LOGGER.error("Unexpected version range parsing error", e);
			return false;
		}
		ArtifactVersion version = new DefaultArtifactVersion(Loader.instance().getIndexedModList().get("NotEnoughItems").getDisplayVersion());
		if(versionRange.containsVersion(version)) {
			return true;
		}
		else {
			LOGGER.warn("NEI Unofficial in version range {} is required for compatibility, was {}. Compatibility will not be present", versionRange, version);
			return false;
		}
	}
}
