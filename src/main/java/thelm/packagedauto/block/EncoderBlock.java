package thelm.packagedauto.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.world.IBlockReader;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.tile.EncoderTile;

public class EncoderBlock extends BaseBlock {

	public static final EncoderBlock INSTANCE = new EncoderBlock();
	public static final Item ITEM_INSTANCE = new BlockItem(INSTANCE, new Item.Properties().group(PackagedAuto.ITEM_GROUP)).setRegistryName("packagedauto:encoder");

	protected EncoderBlock() {
		super(AbstractBlock.Properties.create(Material.IRON).hardnessAndResistance(15F, 25F).sound(SoundType.METAL));
		setRegistryName("packagedauto:encoder");
	}

	@Override
	public EncoderTile createTileEntity(BlockState state, IBlockReader worldIn) {
		return EncoderTile.TYPE_INSTANCE.create();
	}
}
