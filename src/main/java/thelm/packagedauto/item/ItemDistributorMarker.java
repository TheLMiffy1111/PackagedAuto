package thelm.packagedauto.item;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.api.IDistributorMarkerItem;

public class ItemDistributorMarker extends ItemMarker implements IDistributorMarkerItem {

	public static final ItemDistributorMarker INSTANCE = new ItemDistributorMarker();
	public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation("packagedauto:distributor_marker#inventory");
	public static final ModelResourceLocation MODEL_LOCATION_BOUND = new ModelResourceLocation("packagedauto:distributor_marker_bound#inventory");

	protected ItemDistributorMarker() {
		setRegistryName("packagedauto:distributor_marker");
		setTranslationKey("packagedauto.distributor_marker");
		setCreativeTab(PackagedAuto.CREATIVE_TAB);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels() {
		ModelLoader.setCustomMeshDefinition(this, stack->isBound(stack) ? MODEL_LOCATION_BOUND : MODEL_LOCATION);
		ModelBakery.registerItemVariants(this, MODEL_LOCATION, MODEL_LOCATION_BOUND);
	}
}
