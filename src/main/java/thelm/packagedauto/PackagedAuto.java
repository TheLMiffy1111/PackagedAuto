package thelm.packagedauto;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.event.CommonEventHandler;
import thelm.packagedauto.item.ItemPackage;

@Mod(
		modid = PackagedAuto.MOD_ID,
		name = PackagedAuto.NAME,
		version = PackagedAuto.VERSION,
		guiFactory = PackagedAuto.GUI_FACTORY
		)
public class PackagedAuto {

	public static final String MOD_ID = "packagedauto";
	public static final String NAME = "PackagedAuto";
	public static final String VERSION = "1.12.2-0@VERSION@";
	public static final String GUI_FACTORY = "thelm.packagedauto.client.gui.GuiPackagedAutoConfigFactory";
	public static final CreativeTabs CREATIVE_TAB = new CreativeTabs("packagedauto") {
		@SideOnly(Side.CLIENT)
		@Override
		public ItemStack createIcon() {
			return new ItemStack(ItemPackage.INSTANCE);
		}
	};
	@SidedProxy(
			clientSide = "thelm.packagedauto.client.event.ClientEventHandler",
			serverSide = "thelm.packagedauto.event.CommonEventHandler",
			modId = MOD_ID)
	public static CommonEventHandler eventHandler;

	@EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		eventHandler.onPreInit(event);
	}

	@EventHandler
	public void onInit(FMLInitializationEvent event) {
		eventHandler.onInit(event);
	}
}
