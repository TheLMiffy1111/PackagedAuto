package thelm.packagedauto.client.event;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import thelm.packagedauto.client.screen.CrafterScreen;
import thelm.packagedauto.client.screen.EncoderScreen;
import thelm.packagedauto.client.screen.FluidPackageFillerScreen;
import thelm.packagedauto.client.screen.PackagerExtensionScreen;
import thelm.packagedauto.client.screen.PackagerScreen;
import thelm.packagedauto.client.screen.UnpackagerScreen;
import thelm.packagedauto.item.RecipeHolderItem;
import thelm.packagedauto.menu.CrafterMenu;
import thelm.packagedauto.menu.EncoderMenu;
import thelm.packagedauto.menu.FluidPackageFillerMenu;
import thelm.packagedauto.menu.PackagerExtensionMenu;
import thelm.packagedauto.menu.PackagerMenu;
import thelm.packagedauto.menu.UnpackagerMenu;

public class ClientEventHandler {

	public static final ClientEventHandler INSTANCE = new ClientEventHandler();

	public static ClientEventHandler getInstance() {
		return INSTANCE;
	}

	public void onConstruct(IEventBus modEventBus) {
		modEventBus.register(this);
	}

	@SubscribeEvent
	public void onClientSetup(FMLClientSetupEvent event) { 
		event.enqueueWork(()->{
			ItemProperties.register(RecipeHolderItem.INSTANCE,
					new ResourceLocation("packagedauto", "filled"), (stack, world, living, seed)->{
						return stack.hasTag() ? 1F : 0F;
					});
		});
	}

	@SubscribeEvent
	public void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
		event.register(EncoderMenu.TYPE_INSTANCE, EncoderScreen::new);
		event.register(PackagerMenu.TYPE_INSTANCE, PackagerScreen::new);
		event.register(PackagerExtensionMenu.TYPE_INSTANCE, PackagerExtensionScreen::new);
		event.register(UnpackagerMenu.TYPE_INSTANCE, UnpackagerScreen::new);
		event.register(CrafterMenu.TYPE_INSTANCE, CrafterScreen::new);
		event.register(FluidPackageFillerMenu.TYPE_INSTANCE, FluidPackageFillerScreen::new);
	}
}
