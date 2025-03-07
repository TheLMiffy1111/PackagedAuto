package thelm.packagedauto.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Loader;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.client.IModelRegister;

public class ItemMisc extends Item implements IModelRegister {

	public static final ItemMisc PACKAGE_COMPONENT = new ItemMisc("packagedauto:package_component", PackagedAuto.CREATIVE_TAB);
	public static final ItemMisc ME_PACKAGE_COMPONENT = new ItemMisc("packagedauto:me_package_component", Loader.isModLoaded("appliedenergistics2") ? PackagedAuto.CREATIVE_TAB : null);

	public final ModelResourceLocation modelLocation;

	protected ItemMisc(String registryName, CreativeTabs creativeTab) {
		setTranslationKey(registryName.replace(':', '.'));
		setRegistryName(registryName);
		this.modelLocation = new ModelResourceLocation(registryName, "inventory");
		setCreativeTab(creativeTab);
	}

	@Override
	public void registerModels() {
		ModelLoader.setCustomModelResourceLocation(this, 0, modelLocation);
	}
}
