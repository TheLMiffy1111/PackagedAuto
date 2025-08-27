package thelm.packagedauto.integration.jei;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.component.PackagedAutoDataComponents;

public class VolumePackageItemSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {

	@Override
	public Object getSubtypeData(ItemStack ingredient, UidContext context) {
		return ingredient.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK);
	}

	@Override
	public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
		Object data = getSubtypeData(ingredient, context);
		return data == null ? "" : data.toString();
	}
}
