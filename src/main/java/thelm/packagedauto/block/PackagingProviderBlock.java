package thelm.packagedauto.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import thelm.packagedauto.block.entity.BaseBlockEntity;
import thelm.packagedauto.block.entity.PackagedAutoBlockEntities;
import thelm.packagedauto.block.entity.PackagingProviderBlockEntity;

public class PackagingProviderBlock extends BaseBlock {

	protected PackagingProviderBlock() {
		super(BlockBehaviour.Properties.of().strength(10F, 15F).mapColor(MapColor.METAL).sound(SoundType.METAL));
	}

	@Override
	public PackagingProviderBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return PackagedAutoBlockEntities.PACKAGING_PROVIDER.get().create(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return BaseBlockEntity::tick;
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if(state.getBlock() != newState.getBlock()) {
			if(level.getBlockEntity(pos) instanceof PackagingProviderBlockEntity blockEntity) {
				if(blockEntity.currentPattern != null) {
					for(ItemStack stack : blockEntity.currentPattern.getInputs()) {
						if(!stack.isEmpty()) {
							Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
						}
					}
				}
				if(!blockEntity.toSend.isEmpty()) {
					for(ItemStack stack : blockEntity.toSend) {
						if(!stack.isEmpty()) {
							Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
						}
					}
				}
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
		level.getBlockEntity(pos, PackagedAutoBlockEntities.PACKAGING_PROVIDER.get()).ifPresent(PackagingProviderBlockEntity::updatePowered);
	}
}
