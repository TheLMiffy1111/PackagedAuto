package thelm.packagedauto.item;

import net.minecraft.world.item.Item;

public class MiscItem extends Item {

	protected MiscItem() {
		super(new Item.Properties());
	}

	public static final MiscItem PACKAGE_COMPONENT = new MiscItem();
	public static final MiscItem ME_PACKAGE_COMPONENT = new MiscItem();
}
