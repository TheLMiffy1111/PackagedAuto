package thelm.packagedauto.item;

import net.minecraft.world.item.Item;
import thelm.packagedauto.api.IDistributorMarkerItem;

public class DistributorMarkerItem extends MarkerItem implements IDistributorMarkerItem {

	public static final DistributorMarkerItem INSTANCE = new DistributorMarkerItem();

	protected DistributorMarkerItem() {
		super(new Item.Properties());
	}
}
