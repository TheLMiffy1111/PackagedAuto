package thelm.packagedauto.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import thelm.packagedauto.PackagedAuto;

public class MiscItem extends Item {

	protected MiscItem(String registryName, CreativeModeTab creativeTab) {
		super(new Item.Properties().tab(creativeTab));
		setRegistryName(registryName);
	}

	public static final MiscItem PACKAGE_COMPONENT = new MiscItem("packagedauto:package_component", PackagedAuto.CREATIVE_TAB);
	public static final MiscItem ME_PACKAGE_COMPONENT = new MiscItem("packagedauto:me_package_component", PackagedAuto.CREATIVE_TAB);
}
