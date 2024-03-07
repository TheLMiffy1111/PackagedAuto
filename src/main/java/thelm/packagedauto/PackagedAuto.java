package thelm.packagedauto;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import thelm.packagedauto.client.event.ClientEventHandler;
import thelm.packagedauto.event.CommonEventHandler;
import thelm.packagedauto.util.MiscHelper;

@Mod(PackagedAuto.MOD_ID)
public class PackagedAuto {

	public static final String MOD_ID = "packagedauto";

	public PackagedAuto(IEventBus modEventBus) {
		CommonEventHandler.getInstance().onConstruct(modEventBus);
		MiscHelper.INSTANCE.conditionalRunnable(FMLEnvironment.dist::isClient, ()->()->{
			ClientEventHandler.getInstance().onConstruct(modEventBus);
		}, ()->()->{}).run();
	}
}
