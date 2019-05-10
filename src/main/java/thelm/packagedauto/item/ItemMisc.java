package thelm.packagedauto.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.client.IModelRegister;

public class ItemMisc extends Item implements IModelRegister {

	public final ModelResourceLocation modelLocation;

	protected ItemMisc(String registryName, String unlocalizedName, String modelLocation, CreativeTabs creativeTab) {
		setRegistryName(registryName);
		setTranslationKey(unlocalizedName);
		this.modelLocation = new ModelResourceLocation(modelLocation);
		setCreativeTab(creativeTab);
	}

	@Override
	public void registerModels() {
		ModelLoader.setCustomModelResourceLocation(this, 0, modelLocation);
	}

	public static final ItemMisc PACKAGE_COMPONENT = new ItemMisc("packagedauto:package_component", "packagedauto.package_component", "packagedauto:package_component#inventory", PackagedAuto.CREATIVE_TAB);
	public static final ItemMisc ME_PACKAGE_COMPONENT = new ItemMisc("packagedauto:me_package_component", "packagedauto.me_package_component", "packagedauto:me_package_component#inventory", PackagedAuto.CREATIVE_TAB);
}
