package thelm.packagedauto.client.event;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.common.NeoForge;
import thelm.packagedauto.client.WorldOverlayRenderer;
import thelm.packagedauto.client.property.BoundItemModelProperty;
import thelm.packagedauto.client.property.FilledItemModelProperty;
import thelm.packagedauto.client.screen.CrafterScreen;
import thelm.packagedauto.client.screen.CraftingProxyScreen;
import thelm.packagedauto.client.screen.DistributorScreen;
import thelm.packagedauto.client.screen.EncoderScreen;
import thelm.packagedauto.client.screen.FluidPackageFillerScreen;
import thelm.packagedauto.client.screen.PackagerExtensionScreen;
import thelm.packagedauto.client.screen.PackagerScreen;
import thelm.packagedauto.client.screen.PackagingProviderScreen;
import thelm.packagedauto.client.screen.UnpackagerScreen;
import thelm.packagedauto.menu.PackagedAutoMenus;
import thelm.packagedauto.util.MiscHelper;

public class ClientEventHandler {

	public static final ClientEventHandler INSTANCE = new ClientEventHandler();

	public static ClientEventHandler getInstance() {
		return INSTANCE;
	}

	public void onConstruct(IEventBus modEventBus) {
		modEventBus.register(this);
		NeoForge.EVENT_BUS.addListener(this::onRecipesReceived);
		WorldOverlayRenderer.INSTANCE.onConstruct();
	}

	@SubscribeEvent
	public void onClientSetup(FMLClientSetupEvent event) {}

	@SubscribeEvent
	public void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
		event.register(PackagedAutoMenus.ENCODER.get(), EncoderScreen::new);
		event.register(PackagedAutoMenus.PACKAGER.get(), PackagerScreen::new);
		event.register(PackagedAutoMenus.PACKAGER_EXTENSION.get(), PackagerExtensionScreen::new);
		event.register(PackagedAutoMenus.UNPACKAGER.get(), UnpackagerScreen::new);
		event.register(PackagedAutoMenus.DISTRIBUTOR.get(), DistributorScreen::new);
		event.register(PackagedAutoMenus.CRAFTING_PROXY.get(), CraftingProxyScreen::new);
		event.register(PackagedAutoMenus.CRAFTER.get(), CrafterScreen::new);
		event.register(PackagedAutoMenus.FLUID_PACKAGE_FILLER.get(), FluidPackageFillerScreen::new);
		event.register(PackagedAutoMenus.PACKAGING_PROVIDER.get(), PackagingProviderScreen::new);
	}

	@SubscribeEvent
	public void onRegisterConditionalItemModelProperty(RegisterConditionalItemModelPropertyEvent event) {
		event.register(Identifier.parse("packagedauto:filled"), FilledItemModelProperty.MAP_CODEC);
		event.register(Identifier.parse("packagedauto:bound"), BoundItemModelProperty.MAP_CODEC);
	}

	@SubscribeEvent
	public void onRegisterRenderPipelines(RegisterRenderPipelinesEvent event) {
		WorldOverlayRenderer.INSTANCE.onRegisterRenderPipelines(event);
	}

	@SubscribeEvent
	public void onRegisterRenderBuffers(RegisterRenderBuffersEvent event) {
		WorldOverlayRenderer.INSTANCE.onRegisterRenderBuffers(event);
	}

	public void onRecipesReceived(RecipesReceivedEvent event) {
		MiscHelper.INSTANCE.setRecipeMap(event.getRecipeMap());
	}
}
