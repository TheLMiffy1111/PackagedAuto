package thelm.packagedauto.item;

import net.minecraft.item.Item;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.api.IProxyMarkerItem;

public class ProxyMarkerItem extends MarkerItem implements IProxyMarkerItem {

	public static final ProxyMarkerItem INSTANCE = new ProxyMarkerItem();

	protected ProxyMarkerItem() {
		super(new Item.Properties().tab(PackagedAuto.ITEM_GROUP));
		setRegistryName("packagedauto:proxy_marker");
	}
}
