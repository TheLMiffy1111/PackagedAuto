package thelm.packagedauto;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import thelm.packagedauto.item.ItemPackage;
import thelm.packagedauto.proxy.CommonProxy;

@Mod(
		modid = PackagedAuto.MOD_ID,
		name = PackagedAuto.NAME,
		version = PackagedAuto.VERSION,
		dependencies = PackagedAuto.DEPENDENCIES,
		guiFactory = PackagedAuto.GUI_FACTORY
		)
public class PackagedAuto {

	public static final String MOD_ID = "packagedauto";
	public static final String NAME = "PackagedAuto";
	public static final String VERSION = "1.7.10-W.0.1.6";
	public static final String DEPENDENCIES = "";
	public static final String GUI_FACTORY = "thelm.packagedauto.client.gui.GuiPackagedAutoConfigFactory";
	public static final CreativeTabs CREATIVE_TAB = new CreativeTabs("packagedauto") {
		@SideOnly(Side.CLIENT)
		@Override
		public Item getTabIconItem() {
			return ItemPackage.INSTANCE;
		}
	};
	@Instance
	public static PackagedAuto instance;
	@SidedProxy(clientSide = "thelm.packagedauto.proxy.ClientProxy", serverSide = "thelm.packagedauto.proxy.CommonProxy", modId = PackagedAuto.MOD_ID)
	public static CommonProxy proxy;
	public static ModMetadata metadata;

	@EventHandler
	public void firstMovement(FMLPreInitializationEvent event) {
		metadata = event.getModMetadata();
		metadata.autogenerated = false;
		metadata.version = VERSION;
		metadata.authorList.add("TheLMiffy1111");
		metadata.description = "An Applied Energistics 2 addon that uses \"packages\" to allow autocrafting with more than 9 items.";

		proxy.register(event);
	}

	@EventHandler
	public void secondMovement(FMLInitializationEvent event) {
		proxy.register(event);
	}
}
