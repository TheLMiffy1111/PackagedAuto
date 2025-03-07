package thelm.packagedauto.item;

import net.minecraft.item.Item;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.api.IDistributorMarkerItem;

public class DistributorMarkerItem extends MarkerItem implements IDistributorMarkerItem {

	public static final DistributorMarkerItem INSTANCE = new DistributorMarkerItem();

	protected DistributorMarkerItem() {
		super(new Item.Properties().tab(PackagedAuto.ITEM_GROUP));
		setRegistryName("packagedauto:distributor_marker");
	}
}
