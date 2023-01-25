package thelm.packagedauto.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.tile.TileBase;
import thelm.packagedauto.tile.TilePackagerExtension;

public class BlockPackagerExtension extends BlockBase {

	public static final BlockPackagerExtension INSTANCE = new BlockPackagerExtension();
	public static final Item ITEM_INSTANCE = new ItemBlock(INSTANCE);

	@SideOnly(Side.CLIENT)
	protected IIcon topIcon;
	@SideOnly(Side.CLIENT)
	protected IIcon bottomIcon;

	protected BlockPackagerExtension() {
		super(Material.iron);
		setHardness(15F);
		setResistance(25F);
		setStepSound(soundTypeMetal);
		setBlockName("packagedauto.packager_extension");
		setCreativeTab(PackagedAuto.CREATIVE_TAB);
	}

	@Override
	public TileBase createNewTileEntity(World worldIn, int meta) {
		return new TilePackagerExtension();
	}

	@Override
	public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ) {
		TileEntity tileentity = world.getTileEntity(x, y, z);
		if(tileentity instanceof TilePackagerExtension) {
			((TilePackagerExtension)tileentity).updatePowered();
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister reg) {
		blockIcon = reg.registerIcon("packagedauto:packager_side");
		topIcon = reg.registerIcon("packagedauto:packager_extension_top");
		bottomIcon = reg.registerIcon("packagedauto:machine_bottom");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int meta) {
		switch(side) {
		case 0: return bottomIcon;
		case 1: return topIcon;
		default: return blockIcon;
		}
	}
}
