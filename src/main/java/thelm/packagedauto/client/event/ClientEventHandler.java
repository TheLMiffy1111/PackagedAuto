package thelm.packagedauto.client.event;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
import thelm.packagedauto.item.DistributorMarkerItem;
import thelm.packagedauto.item.ProxyMarkerItem;
import thelm.packagedauto.item.RecipeHolderItem;
import thelm.packagedauto.item.SettingsClonerItem;
import thelm.packagedauto.menu.CrafterMenu;
import thelm.packagedauto.menu.CraftingProxyMenu;
import thelm.packagedauto.menu.DistributorMenu;
import thelm.packagedauto.menu.EncoderMenu;
import thelm.packagedauto.menu.FluidPackageFillerMenu;
import thelm.packagedauto.menu.PackagerExtensionMenu;
import thelm.packagedauto.menu.PackagerMenu;
import thelm.packagedauto.menu.PackagingProviderMenu;
import thelm.packagedauto.menu.UnpackagerMenu;

public class ClientEventHandler {

	public static final ClientEventHandler INSTANCE = new ClientEventHandler();

	public static ClientEventHandler getInstance() {
		return INSTANCE;
	}

	@SuppressWarnings("removal")
	public void onConstruct() {
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
		WorldOverlayRenderer.INSTANCE.onConstruct();
	}

	@SubscribeEvent
	public void onClientSetup(FMLClientSetupEvent event) {
		MenuScreens.register(EncoderMenu.TYPE_INSTANCE, EncoderScreen::new);
		MenuScreens.register(PackagerMenu.TYPE_INSTANCE, PackagerScreen::new);
		MenuScreens.register(PackagerExtensionMenu.TYPE_INSTANCE, PackagerExtensionScreen::new);
		MenuScreens.register(UnpackagerMenu.TYPE_INSTANCE, UnpackagerScreen::new);
		MenuScreens.register(DistributorMenu.TYPE_INSTANCE, DistributorScreen::new);
		MenuScreens.register(CraftingProxyMenu.TYPE_INSTANCE, CraftingProxyScreen::new);
		MenuScreens.register(CrafterMenu.TYPE_INSTANCE, CrafterScreen::new);
		MenuScreens.register(FluidPackageFillerMenu.TYPE_INSTANCE, FluidPackageFillerScreen::new);
		MenuScreens.register(PackagingProviderMenu.TYPE_INSTANCE, PackagingProviderScreen::new);

		event.enqueueWork(()->{
			ItemProperties.register(RecipeHolderItem.INSTANCE,
					new ResourceLocation("packagedauto", "filled"), (stack, world, living, seed)->{
						return RecipeHolderItem.INSTANCE.isFilled(stack) ? 1F : 0F;
					});
			ItemProperties.register(DistributorMarkerItem.INSTANCE,
					new ResourceLocation("packagedauto", "bound"), (stack, world, living, seed)->{
						return DistributorMarkerItem.INSTANCE.isBound(stack) ? 1F : 0F;
					});
			ItemProperties.register(ProxyMarkerItem.INSTANCE,
					new ResourceLocation("packagedauto", "bound"), (stack, world, living, seed)->{
						return ProxyMarkerItem.INSTANCE.isBound(stack) ? 1F : 0F;
					});
			ItemProperties.register(SettingsClonerItem.INSTANCE,
					new ResourceLocation("packagedauto", "filled"), (stack, world, living, seed)->{
						return SettingsClonerItem.INSTANCE.hasData(stack) ? 1F : 0F;
					});
		});
	}
}
