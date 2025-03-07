package thelm.packagedauto;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import thelm.packagedauto.event.CommonEventHandler;

@Mod(PackagedAuto.MOD_ID)
public class PackagedAuto {

	public static final String MOD_ID = "packagedauto";

	public PackagedAuto(IEventBus modEventBus, ModContainer modContainer) {
		CommonEventHandler.getInstance().onConstruct(modEventBus, modContainer);
	}
}
