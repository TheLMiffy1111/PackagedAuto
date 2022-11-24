package thelm.packagedauto.client.event;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import thelm.packagedauto.client.screen.CrafterScreen;
import thelm.packagedauto.client.screen.EncoderScreen;
import thelm.packagedauto.client.screen.PackagerExtensionScreen;
import thelm.packagedauto.client.screen.PackagerScreen;
import thelm.packagedauto.client.screen.UnpackagerScreen;
import thelm.packagedauto.container.CrafterContainer;
import thelm.packagedauto.container.EncoderContainer;
import thelm.packagedauto.container.PackagerContainer;
import thelm.packagedauto.container.PackagerExtensionContainer;
import thelm.packagedauto.container.UnpackagerContainer;
import thelm.packagedauto.item.RecipeHolderItem;

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
		ScreenManager.registerFactory(EncoderContainer.TYPE_INSTANCE, EncoderScreen::new);
		ScreenManager.registerFactory(PackagerContainer.TYPE_INSTANCE, PackagerScreen::new);
		ScreenManager.registerFactory(PackagerExtensionContainer.TYPE_INSTANCE, PackagerExtensionScreen::new);
		ScreenManager.registerFactory(UnpackagerContainer.TYPE_INSTANCE, UnpackagerScreen::new);
		ScreenManager.registerFactory(CrafterContainer.TYPE_INSTANCE, CrafterScreen::new);

		event.enqueueWork(()->{
			ItemModelsProperties.registerProperty(RecipeHolderItem.INSTANCE,
					new ResourceLocation("packagedauto", "filled"), (stack, world, living)->{
						return stack.hasTag() ? 1F : 0F;
					});
		});
	}
}
