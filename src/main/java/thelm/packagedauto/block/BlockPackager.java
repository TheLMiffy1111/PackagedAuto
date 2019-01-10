package thelm.packagedauto.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.tile.TileBase;
import thelm.packagedauto.tile.TilePackager;

public class BlockPackager extends BlockBase {

	public static final BlockPackager INSTANCE = new BlockPackager();
	public static final Item ITEM_INSTANCE = new ItemBlock(INSTANCE).setRegistryName("packagedauto:packager");
	public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation("packagedauto:packager#normal");

	protected BlockPackager() {
		super(Material.IRON);
		setHardness(15F);
		setResistance(25F);
		setSoundType(SoundType.METAL);
		setUnlocalizedName("packagedauto.packager");
		setRegistryName("packagedauto:packager");
		setCreativeTab(PackagedAuto.CREATIVE_TAB);
	}

	@Override
	public TileBase createNewTileEntity(World worldIn, int meta) {
		return new TilePackager();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels() {
		ModelLoader.setCustomModelResourceLocation(ITEM_INSTANCE, 0, MODEL_LOCATION);
	}
}
