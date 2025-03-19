package thelm.packagedauto.client.event;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;
import thelm.packagedauto.client.WorldOverlayRenderer;
import thelm.packagedauto.client.screen.CrafterScreen;
import thelm.packagedauto.client.screen.CraftingProxyScreen;
import thelm.packagedauto.client.screen.DistributorScreen;
import thelm.packagedauto.client.screen.EncoderScreen;
import thelm.packagedauto.client.screen.FluidPackageFillerScreen;
import thelm.packagedauto.client.screen.PackagerExtensionScreen;
import thelm.packagedauto.client.screen.PackagerScreen;
import thelm.packagedauto.client.screen.PackagingProviderScreen;
import thelm.packagedauto.client.screen.UnpackagerScreen;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.item.PackagedAutoItems;
import thelm.packagedauto.menu.PackagedAutoMenus;

public class ClientEventHandler {

	public static final ClientEventHandler INSTANCE = new ClientEventHandler();

	public static ClientEventHandler getInstance() {
		return INSTANCE;
	}

	public void onConstruct(IEventBus modEventBus) {
		modEventBus.register(this);
		WorldOverlayRenderer.INSTANCE.onConstruct();
	}

	@SubscribeEvent
	public void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(()->{
			ItemProperties.register(PackagedAutoItems.RECIPE_HOLDER.get(),
					ResourceLocation.parse("packagedauto:filled"), (stack, world, living, seed)->{
						return stack.has(PackagedAutoDataComponents.RECIPE_LIST) ? 1F : 0F;
					});
			ItemProperties.register(PackagedAutoItems.DISTRIBUTOR_MARKER.get(),
					ResourceLocation.parse("packagedauto:bound"), (stack, world, living, seed)->{
						return stack.has(PackagedAutoDataComponents.MARKER_POS) ? 1F : 0F;
					});
			ItemProperties.register(PackagedAutoItems.PROXY_MARKER.get(),
					ResourceLocation.parse("packagedauto:bound"), (stack, world, living, seed)->{
						return stack.has(PackagedAutoDataComponents.MARKER_POS) ? 1F : 0F;
					});
			ItemProperties.register(PackagedAutoItems.SETTINGS_CLONER.get(),
					ResourceLocation.parse("packagedauto:filled"), (stack, world, living, seed)->{
						return stack.has(PackagedAutoDataComponents.CLONER_DATA) ? 1F : 0F;
					});
		});
	}

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
	public void onRegisterRenderBuffers(RegisterRenderBuffersEvent event) {
		WorldOverlayRenderer.INSTANCE.onRegisterRenderBuffers(event);
	}
}
