package thelm.packagedauto.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.tile.TileBase;
import thelm.packagedauto.tile.TileUnpackager;
import thelm.packagedauto.tile.TileUnpackager.PackageTracker;

public class BlockUnpackager extends BlockBase {

	public static final BlockUnpackager INSTANCE = new BlockUnpackager();
	public static final Item ITEM_INSTANCE = new ItemBlock(INSTANCE).setRegistryName("packagedauto:unpackager");
	public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation("packagedauto:unpackager#normal");

	protected BlockUnpackager() {
		super(Material.IRON);
		setHardness(15F);
		setResistance(25F);
		setSoundType(SoundType.METAL);
		setTranslationKey("packagedauto.unpackager");
		setRegistryName("packagedauto:unpackager");
		setCreativeTab(PackagedAuto.CREATIVE_TAB);
	}

	@Override
	public TileBase createNewTileEntity(World worldIn, int meta) {
		return new TileUnpackager();
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if(tileentity instanceof TileUnpackager) {
			for(PackageTracker tracker : ((TileUnpackager)tileentity).trackers) {
				if(!tracker.isEmpty()) {
					if(!tracker.toSend.isEmpty()) {
						for(ItemStack stack : tracker.toSend) {
							if(!stack.isEmpty()) {
								InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
							}
						}
					}
					else {
						List<IPackagePattern> patterns = tracker.recipe.getPatterns();
						for(int i = 0; i < tracker.received.size(); ++i) {
							if(tracker.received.getBoolean(i)) {
								InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), patterns.get(i).getOutput());
							}
						}
					}
				}
			}
		}
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if(tileentity instanceof TileUnpackager) {
			((TileUnpackager)tileentity).updatePowered();
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels() {
		ModelLoader.setCustomModelResourceLocation(ITEM_INSTANCE, 0, MODEL_LOCATION);
	}
}
