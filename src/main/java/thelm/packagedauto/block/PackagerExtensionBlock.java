package thelm.packagedauto.block;

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
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.tile.BaseTile;
import thelm.packagedauto.tile.PackagerExtensionTile;

public class PackagerExtensionBlock extends BaseBlock {

	public static final PackagerExtensionBlock INSTANCE = new PackagerExtensionBlock();
	public static final Item ITEM_INSTANCE = new BlockItem(INSTANCE, new Item.Properties().group(PackagedAuto.ITEM_GROUP)).setRegistryName("packagedauto:packager_extension");

	protected PackagerExtensionBlock() {
		super(AbstractBlock.Properties.create(Material.IRON).hardnessAndResistance(15F, 25F).sound(SoundType.METAL));
		setRegistryName("packagedauto:packager_extension");
	}

	@Override
	public PackagerExtensionTile createTileEntity(BlockState state, IBlockReader worldIn) {
		return PackagerExtensionTile.TYPE_INSTANCE.create();
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if(tileentity instanceof PackagerExtensionTile) {
			((PackagerExtensionTile)tileentity).updatePowered();
		}
	}
}
