package thelm.packagedauto.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import thelm.packagedauto.PackagedAuto;

public class ItemMisc extends Item {

	protected ItemMisc(String unlocalizedName, String textureLocation, CreativeTabs creativeTab) {
		setUnlocalizedName(unlocalizedName);
		setTextureName(textureLocation);
		setCreativeTab(creativeTab);
	}

	public static final ItemMisc PACKAGE_COMPONENT = new ItemMisc("packagedauto.package_component", "packagedauto:package_component", PackagedAuto.CREATIVE_TAB);
	public static final ItemMisc ME_PACKAGE_COMPONENT = new ItemMisc("packagedauto.me_package_component", "packagedauto:me_package_component", PackagedAuto.CREATIVE_TAB);
}
