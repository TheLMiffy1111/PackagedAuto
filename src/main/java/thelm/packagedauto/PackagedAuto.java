package thelm.packagedauto;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import thelm.packagedauto.client.event.ClientEventHandler;
import thelm.packagedauto.event.CommonEventHandler;
import thelm.packagedauto.item.PackageItem;

@Mod(PackagedAuto.MOD_ID)
public class PackagedAuto {

	public static final String MOD_ID = "packagedauto";
	public static final CreativeModeTab CREATIVE_TAB = new CreativeModeTab("packagedauto") {
		@Override
		public ItemStack makeIcon() {
			return new ItemStack(PackageItem.INSTANCE);
		}
	};
	public static PackagedAuto core;

	public PackagedAuto() {
		core = this;
		CommonEventHandler.getInstance().onConstruct();
		DistExecutor.runWhenOn(Dist.CLIENT, ()->()->{
			ClientEventHandler.getInstance().onConstruct();
		});
	}
}
