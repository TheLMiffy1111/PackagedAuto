package thelm.packagedauto.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.redstone.Orientation;
import thelm.packagedauto.block.entity.BaseBlockEntity;
import thelm.packagedauto.block.entity.FluidPackageFillerBlockEntity;
import thelm.packagedauto.block.entity.PackagedAutoBlockEntities;

public class FluidPackageFillerBlock extends BaseBlock {

	protected FluidPackageFillerBlock(Properties properties) {
		super(properties.strength(10F, 15F).mapColor(MapColor.METAL).sound(SoundType.METAL));
	}

	@Override
	public FluidPackageFillerBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return PackagedAutoBlockEntities.FLUID_PACKAGE_FILLER.get().create(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return BaseBlockEntity::tick;
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean movedByPiston) {
		level.getBlockEntity(pos, PackagedAutoBlockEntities.FLUID_PACKAGE_FILLER.get()).ifPresent(FluidPackageFillerBlockEntity::updatePowered);
	}
}
