package thelm.packagedauto.integration.appeng;

import appeng.api.AECapabilities;
import appeng.api.crafting.PatternDetailsHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import thelm.packagedauto.block.entity.PackagedAutoBlockEntities;
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

	@SubscribeEvent
	public void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, PackagedAutoBlockEntities.PACKAGER.get(), (be, v)->AppEngUtil.getAsInWorldGridNodeHost(be));
		event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, PackagedAutoBlockEntities.PACKAGER_EXTENSION.get(), (be, v)->AppEngUtil.getAsInWorldGridNodeHost(be));
		event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, PackagedAutoBlockEntities.UNPACKAGER.get(), (be, v)->AppEngUtil.getAsInWorldGridNodeHost(be));
		event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, PackagedAutoBlockEntities.DISTRIBUTOR.get(), (be, v)->AppEngUtil.getAsInWorldGridNodeHost(be));
		event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, PackagedAutoBlockEntities.CRAFTING_PROXY.get(), (be, v)->AppEngUtil.getAsInWorldGridNodeHost(be));
		event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, PackagedAutoBlockEntities.CRAFTER.get(), (be, v)->AppEngUtil.getAsInWorldGridNodeHost(be));
		event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, PackagedAutoBlockEntities.PACKAGING_PROVIDER.get(), (be, v)->AppEngUtil.getAsInWorldGridNodeHost(be));
	}
}
