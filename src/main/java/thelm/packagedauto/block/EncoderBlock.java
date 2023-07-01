package thelm.packagedauto.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import thelm.packagedauto.block.entity.EncoderBlockEntity;

public class EncoderBlock extends BaseBlock {

	public static final EncoderBlock INSTANCE = new EncoderBlock();
	public static final Item ITEM_INSTANCE = new BlockItem(INSTANCE, new Item.Properties());

	protected EncoderBlock() {
		super(BlockBehaviour.Properties.of().strength(15F, 25F).mapColor(MapColor.METAL).sound(SoundType.METAL));
	}

	@Override
	public EncoderBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return EncoderBlockEntity.TYPE_INSTANCE.create(pos, state);
	}
}
