package thelm.packagedauto.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import thelm.packagedauto.block.entity.EncoderBlockEntity;
import thelm.packagedauto.block.entity.PackagedAutoBlockEntities;

public class EncoderBlock extends BaseBlock {

	protected EncoderBlock(Properties properties) {
		super(properties.strength(10F, 15F).mapColor(MapColor.METAL).sound(SoundType.METAL));
	}

	@Override
	public EncoderBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return PackagedAutoBlockEntities.ENCODER.get().create(pos, state);
	}
}
