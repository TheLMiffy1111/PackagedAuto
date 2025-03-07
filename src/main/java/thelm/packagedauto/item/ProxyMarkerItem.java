package thelm.packagedauto.item;

import net.minecraft.world.item.Item;
import thelm.packagedauto.api.IProxyMarkerItem;

public class ProxyMarkerItem extends MarkerItem implements IProxyMarkerItem {

	public static final ProxyMarkerItem INSTANCE = new ProxyMarkerItem();

	protected ProxyMarkerItem() {
		super(new Item.Properties());
	}
}
