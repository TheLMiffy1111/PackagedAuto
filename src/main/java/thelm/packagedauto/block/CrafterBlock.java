package thelm.packagedauto.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.world.IBlockReader;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.tile.CrafterTile;

public class CrafterBlock extends BaseBlock {

	public static final CrafterBlock INSTANCE = new CrafterBlock();
	public static final Item ITEM_INSTANCE = new BlockItem(INSTANCE, new Item.Properties().group(PackagedAuto.ITEM_GROUP)).setRegistryName("packagedauto:crafter");

	protected CrafterBlock() {
		super(AbstractBlock.Properties.create(Material.IRON).hardnessAndResistance(15F, 25F).sound(SoundType.METAL));
		setRegistryName("packagedauto:crafter");
	}

	@Override
	public CrafterTile createTileEntity(BlockState state, IBlockReader worldIn) {
		return new CrafterTile();
	}
}
