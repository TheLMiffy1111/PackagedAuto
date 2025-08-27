package thelm.packagedauto.integration.jei;

import java.util.List;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.component.PackagedAutoDataComponents;

public class PackageItemSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {

	@Override
	public Object getSubtypeData(ItemStack ingredient, UidContext context) {
		return List.of(ingredient.get(PackagedAutoDataComponents.RECIPE), ingredient.get(PackagedAutoDataComponents.PACKAGE_INDEX));
	}

	@Override
	public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
		Object data = getSubtypeData(ingredient, context);
		return data == null ? "" : data.toString();
	}
}
