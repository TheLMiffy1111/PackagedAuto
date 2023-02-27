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
import thelm.packagedauto.tile.PackagerExtensionTile;

public class PackagerExtensionBlock extends BaseBlock {

	public static final PackagerExtensionBlock INSTANCE = new PackagerExtensionBlock();
	public static final Item ITEM_INSTANCE = new BlockItem(INSTANCE, new Item.Properties().tab(PackagedAuto.ITEM_GROUP)).setRegistryName("packagedauto:packager_extension");

	protected PackagerExtensionBlock() {
		super(AbstractBlock.Properties.of(Material.METAL).strength(15F, 25F).sound(SoundType.METAL));
		setRegistryName("packagedauto:packager_extension");
	}

	@Override
	public PackagerExtensionTile createTileEntity(BlockState state, IBlockReader worldIn) {
		return PackagerExtensionTile.TYPE_INSTANCE.create();
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		TileEntity tileentity = worldIn.getBlockEntity(pos);
		if(tileentity instanceof PackagerExtensionTile) {
			((PackagerExtensionTile)tileentity).updatePowered();
		}
	}
}
