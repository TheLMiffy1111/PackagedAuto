package thelm.packagedauto.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.tile.UnpackagerTile;
import thelm.packagedauto.tile.UnpackagerTile.PackageTracker;

public class UnpackagerBlock extends BaseBlock {

	public static final UnpackagerBlock INSTANCE = new UnpackagerBlock();
	public static final Item ITEM_INSTANCE = new BlockItem(INSTANCE, new Item.Properties().tab(PackagedAuto.ITEM_GROUP)).setRegistryName("packagedauto:unpackager");

	protected UnpackagerBlock() {
		super(AbstractBlock.Properties.of(Material.METAL).strength(10F, 15F).sound(SoundType.METAL));
		setRegistryName("packagedauto:unpackager");
	}

	@Override
	public UnpackagerTile createTileEntity(BlockState state, IBlockReader worldIn) {
		return UnpackagerTile.TYPE_INSTANCE.create();
	}

	@Override
	public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if(state.getBlock() != newState.getBlock()) {
			TileEntity tileentity = worldIn.getBlockEntity(pos);
			if(tileentity instanceof UnpackagerTile) {
				for(PackageTracker tracker : ((UnpackagerTile)tileentity).trackers) {
					tracker.ejectItems();
				}
			}
		}
		super.onRemove(state, worldIn, pos, newState, isMoving);
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		TileEntity tileentity = worldIn.getBlockEntity(pos);
		if(tileentity instanceof UnpackagerTile) {
			((UnpackagerTile)tileentity).updatePowered();
		}
	}
}
