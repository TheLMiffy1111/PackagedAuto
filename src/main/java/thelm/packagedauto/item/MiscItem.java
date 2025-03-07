package thelm.packagedauto.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.ModList;
import thelm.packagedauto.PackagedAuto;

public class MiscItem extends Item {

	public static final MiscItem PACKAGE_COMPONENT = new MiscItem("packagedauto:package_component", PackagedAuto.ITEM_GROUP);
	public static final MiscItem ME_PACKAGE_COMPONENT = new MiscItem("packagedauto:me_package_component", ModList.get().isLoaded("appliedenergistics2") ? PackagedAuto.ITEM_GROUP : null);

	protected MiscItem(String registryName, ItemGroup itemGroup) {
		super(new Item.Properties().tab(itemGroup));
		setRegistryName(registryName);
	}
}
