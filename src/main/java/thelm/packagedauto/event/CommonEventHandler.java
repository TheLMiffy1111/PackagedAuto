package thelm.packagedauto.event;

import appeng.api.AECapabilities;
import appeng.api.crafting.PatternDetailsHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import thelm.packagedauto.api.IVolumeType;
import thelm.packagedauto.block.PackagedAutoBlocks;
import thelm.packagedauto.block.entity.BaseBlockEntity;
import thelm.packagedauto.block.entity.PackagedAutoBlockEntities;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.config.PackagedAutoConfig;
import thelm.packagedauto.crafting.PackagedAutoRecipeSerializers;
import thelm.packagedauto.creativetab.PackagedAutoCreativeTabs;
import thelm.packagedauto.integration.appeng.AppEngUtil;
import thelm.packagedauto.integration.appeng.recipe.PackagePatternDetailsDecoder;
import thelm.packagedauto.item.PackagedAutoItems;
import thelm.packagedauto.menu.PackagedAutoMenus;
import thelm.packagedauto.packet.BeamPacket;
import thelm.packagedauto.packet.ChangeBlockingPacket;
import thelm.packagedauto.packet.ChangePackagingPacket;
import thelm.packagedauto.packet.ChangeProvidingPacket;
import thelm.packagedauto.packet.CycleRecipeTypePacket;
import thelm.packagedauto.packet.DirectionalMarkerPacket;
import thelm.packagedauto.packet.EjectTrackerPacket;
import thelm.packagedauto.packet.LoadRecipeListPacket;
import thelm.packagedauto.packet.SaveRecipeListPacket;
import thelm.packagedauto.packet.SetFluidAmountPacket;
import thelm.packagedauto.packet.SetItemStackPacket;
import thelm.packagedauto.packet.SetPatternIndexPacket;
import thelm.packagedauto.packet.SetRecipePacket;
import thelm.packagedauto.packet.SizedMarkerPacket;
import thelm.packagedauto.packet.SyncEnergyPacket;
import thelm.packagedauto.packet.TrackerCountPacket;
import thelm.packagedauto.recipe.CraftingPackageRecipeType;
import thelm.packagedauto.recipe.OrderedProcessingPackageRecipeType;
import thelm.packagedauto.recipe.PositionedProcessingPackageRecipeType;
import thelm.packagedauto.recipe.ProcessingPackageRecipeType;
import thelm.packagedauto.util.ApiImpl;
import thelm.packagedauto.util.MiscHelper;
import thelm.packagedauto.volume.FluidVolumeType;

public class CommonEventHandler {

	public static final CommonEventHandler INSTANCE = new CommonEventHandler();

	public static CommonEventHandler getInstance() {
		return INSTANCE;
	}

	public void onConstruct(IEventBus modEventBus, ModContainer modContainer) {
		modEventBus.register(this);
		NeoForge.EVENT_BUS.addListener(this::onServerAboutToStart);
		PackagedAutoConfig.registerConfig(modContainer);

		PackagedAutoBlocks.BLOCKS.register(modEventBus);
		PackagedAutoItems.ITEMS.register(modEventBus);
		PackagedAutoBlockEntities.BLOCK_ENTITIES.register(modEventBus);
		PackagedAutoMenus.MENUS.register(modEventBus);
		PackagedAutoDataComponents.DATA_COMPONENTS.register(modEventBus);
		PackagedAutoRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
		PackagedAutoCreativeTabs.CREATIVE_TABS.register(modEventBus);
	}

	@SubscribeEvent
	public void onCommonSetup(FMLCommonSetupEvent event) {
		ApiImpl.INSTANCE.registerVolumeType(FluidVolumeType.INSTANCE);

		ApiImpl.INSTANCE.registerRecipeType(ProcessingPackageRecipeType.INSTANCE);
		ApiImpl.INSTANCE.registerRecipeType(OrderedProcessingPackageRecipeType.INSTANCE);
		ApiImpl.INSTANCE.registerRecipeType(PositionedProcessingPackageRecipeType.INSTANCE);
		ApiImpl.INSTANCE.registerRecipeType(CraftingPackageRecipeType.INSTANCE);

		MiscHelper.INSTANCE.conditionalRunnable(()->ModList.get().isLoaded("ae2"), ()->()->{
			PatternDetailsHelper.registerDecoder(PackagePatternDetailsDecoder.INSTANCE);
		}, ()->()->{}).run();
	}

	@SubscribeEvent
	public void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PackagedAutoBlockEntities.PACKAGER.get(), BaseBlockEntity::getItemHandler);
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PackagedAutoBlockEntities.PACKAGER_EXTENSION.get(), BaseBlockEntity::getItemHandler);
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PackagedAutoBlockEntities.UNPACKAGER.get(), BaseBlockEntity::getItemHandler);
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PackagedAutoBlockEntities.CRAFTER.get(), BaseBlockEntity::getItemHandler);
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PackagedAutoBlockEntities.FLUID_PACKAGE_FILLER.get(), BaseBlockEntity::getItemHandler);

		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, PackagedAutoBlockEntities.PACKAGER.get(), BaseBlockEntity::getEnergyStorage);
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, PackagedAutoBlockEntities.PACKAGER_EXTENSION.get(), BaseBlockEntity::getEnergyStorage);
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, PackagedAutoBlockEntities.UNPACKAGER.get(), BaseBlockEntity::getEnergyStorage);
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, PackagedAutoBlockEntities.CRAFTER.get(), BaseBlockEntity::getEnergyStorage);
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, PackagedAutoBlockEntities.FLUID_PACKAGE_FILLER.get(), BaseBlockEntity::getEnergyStorage);

		MiscHelper.INSTANCE.conditionalRunnable(()->ModList.get().isLoaded("ae2"), ()->()->{
			event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, PackagedAutoBlockEntities.PACKAGER.get(), (be, v)->AppEngUtil.getAsInWorldGridNodeHost(be));
			event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, PackagedAutoBlockEntities.PACKAGER_EXTENSION.get(), (be, v)->AppEngUtil.getAsInWorldGridNodeHost(be));
			event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, PackagedAutoBlockEntities.UNPACKAGER.get(), (be, v)->AppEngUtil.getAsInWorldGridNodeHost(be));
			event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, PackagedAutoBlockEntities.DISTRIBUTOR.get(), (be, v)->AppEngUtil.getAsInWorldGridNodeHost(be));
			event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, PackagedAutoBlockEntities.CRAFTING_PROXY.get(), (be, v)->AppEngUtil.getAsInWorldGridNodeHost(be));
			event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, PackagedAutoBlockEntities.CRAFTER.get(), (be, v)->AppEngUtil.getAsInWorldGridNodeHost(be));
			event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, PackagedAutoBlockEntities.PACKAGING_PROVIDER.get(), (be, v)->AppEngUtil.getAsInWorldGridNodeHost(be));
		}, ()->()->{}).run();

		for(IVolumeType volumeType : ApiImpl.INSTANCE.getVolumeTypeRegistry().values()) {
			event.registerItem(volumeType.getItemCapability(), (stack, ctx)->{
				if(stack.has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK) && volumeType == stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK).getVolumeType()) {
					return volumeType.makeItemCapability(stack);
				}
				return null;
			}, PackagedAutoItems.VOLUME_PACKAGE);
		}
	}

	@SubscribeEvent
	public void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar("packagedauto");
		registrar.playToClient(SyncEnergyPacket.TYPE, SyncEnergyPacket.STREAM_CODEC, SyncEnergyPacket::handle);
		registrar.playToServer(SetItemStackPacket.TYPE, SetItemStackPacket.STREAM_CODEC, SetItemStackPacket::handle);
		registrar.playToServer(SetPatternIndexPacket.TYPE, SetPatternIndexPacket.STREAM_CODEC, SetPatternIndexPacket::handle);
		registrar.playToServer(CycleRecipeTypePacket.TYPE, CycleRecipeTypePacket.STREAM_CODEC, CycleRecipeTypePacket::handle);
		registrar.playToServer(SaveRecipeListPacket.TYPE, SaveRecipeListPacket.STREAM_CODEC, SaveRecipeListPacket::handle);
		registrar.playToServer(SetRecipePacket.TYPE, SetRecipePacket.STREAM_CODEC, SetRecipePacket::handle);
		registrar.playToServer(LoadRecipeListPacket.TYPE, LoadRecipeListPacket.STREAM_CODEC, LoadRecipeListPacket::handle);
		registrar.playToServer(ChangeBlockingPacket.TYPE, ChangeBlockingPacket.STREAM_CODEC, ChangeBlockingPacket::handle);
		registrar.playToServer(SetFluidAmountPacket.TYPE, SetFluidAmountPacket.STREAM_CODEC, SetFluidAmountPacket::handle);
		registrar.playToServer(ChangePackagingPacket.TYPE, ChangePackagingPacket.STREAM_CODEC, ChangePackagingPacket::handle);
		registrar.playToServer(TrackerCountPacket.TYPE, TrackerCountPacket.STREAM_CODEC, TrackerCountPacket::handle);
		registrar.playToClient(BeamPacket.TYPE, BeamPacket.STREAM_CODEC, BeamPacket::handle);
		registrar.playToClient(DirectionalMarkerPacket.TYPE, DirectionalMarkerPacket.STREAM_CODEC, DirectionalMarkerPacket::handle);
		registrar.playToClient(SizedMarkerPacket.TYPE, SizedMarkerPacket.STREAM_CODEC, SizedMarkerPacket::handle);
		registrar.playToServer(ChangeProvidingPacket.TYPE, ChangeProvidingPacket.STREAM_CODEC, ChangeProvidingPacket::handle);
		registrar.playToServer(EjectTrackerPacket.TYPE, EjectTrackerPacket.STREAM_CODEC, EjectTrackerPacket::handle);
	}

	@SubscribeEvent
	public void onModConfigLoading(ModConfigEvent.Loading event) {
		switch(event.getConfig().getType()) {
		case SERVER -> PackagedAutoConfig.reloadServerConfig();
		default -> {}
		}
	}

	@SubscribeEvent
	public void onModConfigReloading(ModConfigEvent.Reloading event) {
		switch(event.getConfig().getType()) {
		case SERVER -> PackagedAutoConfig.reloadServerConfig();
		default -> {}
		}
	}

	public void onServerAboutToStart(ServerAboutToStartEvent event) {
		MiscHelper.INSTANCE.setServer(event.getServer());
	}
}
