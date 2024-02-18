package thelm.packagedauto;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.item.ItemPackage;
import thelm.packagedauto.proxy.CommonProxy;

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
			clientSide = "thelm.packagedauto.proxy.ClientProxy",
			serverSide = "thelm.packagedauto.proxy.CommonProxy",
			modId = PackagedAuto.MOD_ID)
	public static CommonProxy proxy;

	@EventHandler
	public void firstMovement(FMLPreInitializationEvent event) {
		proxy.register(event);
	}
}
