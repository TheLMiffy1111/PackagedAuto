package thelm.packagedauto.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;
import thelm.packagedauto.PackagedAuto;

public class MiscItem extends Item {

	public static final MiscItem PACKAGE_COMPONENT = new MiscItem(PackagedAuto.CREATIVE_TAB);
	public static final MiscItem ME_PACKAGE_COMPONENT = new MiscItem(ModList.get().isLoaded("ae2") ? PackagedAuto.CREATIVE_TAB : null);

	protected MiscItem(CreativeModeTab creativeTab) {
		super(new Item.Properties().tab(creativeTab));
	}
}
