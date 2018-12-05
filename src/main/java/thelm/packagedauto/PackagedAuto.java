package thelm.packagedauto;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import thelm.packagedauto.proxy.CommonProxy;

@Mod(
		modid = PackagedAuto.MOD_ID,
		name = PackagedAuto.NAME,
		version = PackagedAuto.VERSION,
		dependencies = PackagedAuto.DEPENDENCIES
		)
public class PackagedAuto {

	public static final String MOD_ID = "packagedauto";
	public static final String NAME = "PackagedAuto";
	public static final String VERSION = "1.12.2-1.0.0.0";
	public static final String DEPENDENCIES = "";
	@Instance
	public static PackagedAuto instance;
	@SidedProxy(clientSide = "thelm.packagedauto.proxy.ClientProxy", serverSide = "thelm.packagedauto.proxy.CommonProxy", modId = PackagedAuto.MOD_ID)
	public static CommonProxy proxy;
	public static ModMetadata metadata;

	@EventHandler
	public void firstMovement(FMLPreInitializationEvent event) {
		proxy.registerBlocks();
		proxy.registerItems();
		proxy.registerModels();
		proxy.registerTileEntities();
		proxy.registerMisc();
	}
}
