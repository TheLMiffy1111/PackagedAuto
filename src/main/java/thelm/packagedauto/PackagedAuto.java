package thelm.packagedauto;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import thelm.packagedauto.client.event.ClientEventHandler;
import thelm.packagedauto.event.CommonEventHandler;
import thelm.packagedauto.item.PackageItem;

@Mod(PackagedAuto.MOD_ID)
public class PackagedAuto {

	public static final String MOD_ID = "packagedauto";
	public static final ItemGroup ITEM_GROUP = new ItemGroup("packagedauto") {
		@OnlyIn(Dist.CLIENT)
		@Override
		public ItemStack createIcon() {
			return new ItemStack(PackageItem.INSTANCE);
		}
	};
	public static PackagedAuto core;

	public PackagedAuto() {
		core = this;
		FMLJavaModLoadingContext.get().getModEventBus().register(CommonEventHandler.getInstance());
		MinecraftForge.EVENT_BUS.addListener(CommonEventHandler.getInstance()::onServerAboutToStart);
		DistExecutor.runWhenOn(Dist.CLIENT, ()->()->{
			FMLJavaModLoadingContext.get().getModEventBus().register(ClientEventHandler.getInstance());
		});
	}
}
