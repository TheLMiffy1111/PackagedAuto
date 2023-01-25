package thelm.packagedauto.recipe;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import thelm.packagedauto.api.IPackageRecipeInfo;

public class PackageRecipeTypeProcessingOrdered extends PackageRecipeTypeProcessing {

	public static final PackageRecipeTypeProcessingOrdered INSTANCE = new PackageRecipeTypeProcessingOrdered();

	@Override
	public String getName() {
		return "packagedauto:processing_ordered";
	}

	@Override
	public String getLocalizedName() {
		return StatCollector.translateToLocal("recipe.packagedauto.processing_ordered");
	}

	@Override
	public String getLocalizedNameShort() {
		return StatCollector.translateToLocal("recipe.packagedauto.processing_ordered.short");
	}

	@Override
	public IPackageRecipeInfo getNewRecipeInfo() {
		return new PackageRecipeInfoProcessingOrdered();
	}
}
