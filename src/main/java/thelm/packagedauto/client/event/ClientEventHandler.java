package thelm.packagedauto.client.event;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import thelm.packagedauto.client.screen.CrafterScreen;
import thelm.packagedauto.client.screen.EncoderScreen;
import thelm.packagedauto.client.screen.PackagerExtensionScreen;
import thelm.packagedauto.client.screen.PackagerScreen;
import thelm.packagedauto.client.screen.UnpackagerScreen;
import thelm.packagedauto.item.RecipeHolderItem;
import thelm.packagedauto.menu.CrafterMenu;
import thelm.packagedauto.menu.EncoderMenu;
import thelm.packagedauto.menu.PackagerExtensionMenu;
import thelm.packagedauto.menu.PackagerMenu;
import thelm.packagedauto.menu.UnpackagerMenu;

public class ClientEventHandler {

	public static final ClientEventHandler INSTANCE = new ClientEventHandler();

	public static ClientEventHandler getInstance() {
		return INSTANCE;
	}
	
	public void onConstruct() {
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@SubscribeEvent
	public void onClientSetup(FMLClientSetupEvent event) {
		MenuScreens.register(EncoderMenu.TYPE_INSTANCE, EncoderScreen::new);
		MenuScreens.register(PackagerMenu.TYPE_INSTANCE, PackagerScreen::new);
		MenuScreens.register(PackagerExtensionMenu.TYPE_INSTANCE, PackagerExtensionScreen::new);
		MenuScreens.register(UnpackagerMenu.TYPE_INSTANCE, UnpackagerScreen::new);
		MenuScreens.register(CrafterMenu.TYPE_INSTANCE, CrafterScreen::new);

		event.enqueueWork(()->{
			ItemProperties.register(RecipeHolderItem.INSTANCE, 
					new ResourceLocation("packagedauto", "filled"), (stack, world, living, seed)->{
						return stack.hasTag() ? 1F : 0F;
					});
		});
	}
}
