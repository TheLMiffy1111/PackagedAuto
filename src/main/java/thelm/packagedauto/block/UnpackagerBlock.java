package thelm.packagedauto.block;

import java.util.List;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.tile.UnpackagerTile;
import thelm.packagedauto.tile.UnpackagerTile.PackageTracker;

public class UnpackagerBlock extends BaseBlock {

	public static final UnpackagerBlock INSTANCE = new UnpackagerBlock();
	public static final Item ITEM_INSTANCE = new BlockItem(INSTANCE, new Item.Properties().tab(PackagedAuto.ITEM_GROUP)).setRegistryName("packagedauto:unpackager");

	protected UnpackagerBlock() {
		super(AbstractBlock.Properties.of(Material.METAL).strength(15F, 25F).sound(SoundType.METAL));
		setRegistryName("packagedauto:unpackager");
	}

	@Override
	public UnpackagerTile createTileEntity(BlockState state, IBlockReader worldIn) {
		return UnpackagerTile.TYPE_INSTANCE.create();
	}

	@Override
	public void destroy(IWorld worldIn, BlockPos pos, BlockState state) {
		TileEntity tileentity = worldIn.getBlockEntity(pos);
		if(tileentity instanceof UnpackagerTile) {
			for(PackageTracker tracker : ((UnpackagerTile)tileentity).trackers) {
				if(!tracker.isEmpty()) {
					if(!tracker.toSend.isEmpty()) {
						for(ItemStack stack : tracker.toSend) {
							if(!stack.isEmpty()) {
								InventoryHelper.dropItemStack((World)worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
							}
						}
					}
					else {
						List<IPackagePattern> patterns = tracker.recipe.getPatterns();
						for(int i = 0; i < tracker.received.size() && i < patterns.size(); ++i) {
							if(tracker.received.getBoolean(i)) {
								InventoryHelper.dropItemStack((World)worldIn, pos.getX(), pos.getY(), pos.getZ(), patterns.get(i).getOutput());
							}
						}
					}
				}
			}
		}
		super.destroy(worldIn, pos, state);
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		TileEntity tileentity = worldIn.getBlockEntity(pos);
		if(tileentity instanceof UnpackagerTile) {
			((UnpackagerTile)tileentity).updatePowered();
		}
	}
}
