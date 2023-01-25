package thelm.packagedauto.block;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.tile.TileBase;
import thelm.packagedauto.tile.TileUnpackager;
import thelm.packagedauto.tile.TileUnpackager.PackageTracker;

public class BlockUnpackager extends BlockBase {

	public static final BlockUnpackager INSTANCE = new BlockUnpackager();
	public static final Item ITEM_INSTANCE = new ItemBlock(INSTANCE);

	@SideOnly(Side.CLIENT)
	protected IIcon topIcon;
	@SideOnly(Side.CLIENT)
	protected IIcon bottomIcon;

	protected BlockUnpackager() {
		super(Material.iron);
		setHardness(15F);
		setResistance(25F);
		setStepSound(soundTypeMetal);
		setBlockName("packagedauto.unpackager");
		setCreativeTab(PackagedAuto.CREATIVE_TAB);
	}

	@Override
	public TileBase createNewTileEntity(World worldIn, int meta) {
		return new TileUnpackager();
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		TileEntity tileentity = world.getTileEntity(x, y, z);
		if(tileentity instanceof TileUnpackager) {
			for(PackageTracker tracker : ((TileUnpackager)tileentity).trackers) {
				if(!tracker.isEmpty()) {
					if(!tracker.toSend.isEmpty()) {
						for(ItemStack stack : tracker.toSend) {
							spawnItemStack(world, x, y, z, stack);
						}
					}
					else {
						List<IPackagePattern> patterns = tracker.recipe.getPatterns();
						for(int i = 0; i < tracker.received.size() && i < patterns.size(); ++i) {
							if(tracker.received.get(i)) {
								spawnItemStack(world, x, y, z, patterns.get(i).getOutput());
							}
						}
					}
				}
			}
		}
		super.breakBlock(world, x, y, z, block, meta);
	}

	@Override
	public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ) {
		TileEntity tileentity = world.getTileEntity(x, y, z);
		if(tileentity instanceof TileUnpackager) {
			((TileUnpackager)tileentity).updatePowered();
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister reg) {
		blockIcon = reg.registerIcon("packagedauto:unpackager_side");
		topIcon = reg.registerIcon("packagedauto:unpackager_top");
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
