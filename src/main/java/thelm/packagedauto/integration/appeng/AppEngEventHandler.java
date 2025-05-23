package thelm.packagedauto.integration.appeng;

import appeng.api.crafting.PatternDetailsHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import thelm.packagedauto.integration.appeng.recipe.PackagePatternDetailsDecoder;

public class AppEngEventHandler {

	public static final AppEngEventHandler INSTANCE = new AppEngEventHandler();

	public static AppEngEventHandler getInstance() {
		return INSTANCE;
	}

	@SubscribeEvent
	public void onCommonSetup(FMLCommonSetupEvent event) {
		PatternDetailsHelper.registerDecoder(PackagePatternDetailsDecoder.INSTANCE);
	}
}
