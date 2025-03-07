package thelm.packagedauto.item;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.api.IProxyMarkerItem;

public class ItemProxyMarker extends ItemMarker implements IProxyMarkerItem {

	public static final ItemProxyMarker INSTANCE = new ItemProxyMarker();
	public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation("packagedauto:proxy_marker#inventory");
	public static final ModelResourceLocation MODEL_LOCATION_BOUND = new ModelResourceLocation("packagedauto:proxy_marker_bound#inventory");

	protected ItemProxyMarker() {
		setRegistryName("packagedauto:proxy_marker");
		setTranslationKey("packagedauto.proxy_marker");
		setCreativeTab(PackagedAuto.CREATIVE_TAB);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels() {
		ModelLoader.setCustomMeshDefinition(this, stack->isBound(stack) ? MODEL_LOCATION_BOUND : MODEL_LOCATION);
		ModelBakery.registerItemVariants(this, MODEL_LOCATION, MODEL_LOCATION_BOUND);
	}
}
