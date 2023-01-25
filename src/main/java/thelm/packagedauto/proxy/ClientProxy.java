package thelm.packagedauto.proxy;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import thelm.packagedauto.integration.nei.NEIHandler;

public class ClientProxy extends CommonProxy {

	@Optional.Method(modid="NotEnoughItems")
	@Override
	protected void registerNEI() {
		if(Loader.isModLoaded("NotEnoughItems")) {
			NEIHandler.INSTANCE.register();
		}
	}
}
