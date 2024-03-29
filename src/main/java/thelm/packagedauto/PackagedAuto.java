package thelm.packagedauto;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import thelm.packagedauto.client.event.ClientEventHandler;
import thelm.packagedauto.event.CommonEventHandler;

@Mod(PackagedAuto.MOD_ID)
public class PackagedAuto {

	public static final String MOD_ID = "packagedauto";

	public PackagedAuto() {
		CommonEventHandler.getInstance().onConstruct();
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ()->()->{
			ClientEventHandler.getInstance().onConstruct();
		});
	}
}
