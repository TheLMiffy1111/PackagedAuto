package thelm.packagedauto.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.block.entity.BaseBlockEntity;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity.PackageTracker;

public class UnpackagerBlock extends BaseBlock {

	public static final UnpackagerBlock INSTANCE = new UnpackagerBlock();
	public static final Item ITEM_INSTANCE = new BlockItem(INSTANCE, new Item.Properties().tab(PackagedAuto.CREATIVE_TAB));

	protected UnpackagerBlock() {
		super(BlockBehaviour.Properties.of(Material.METAL).strength(10F, 15F).sound(SoundType.METAL));
	}

	@Override
	public UnpackagerBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return UnpackagerBlockEntity.TYPE_INSTANCE.create(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return BaseBlockEntity::tick;
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if(state.getBlock() != newState.getBlock()) {
			if(level.getBlockEntity(pos) instanceof UnpackagerBlockEntity unpackager) {
				for(PackageTracker tracker : unpackager.trackers) {
					if(!tracker.isEmpty()) {
						tracker.ejectItems();
					}
				}
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
		level.getBlockEntity(pos, UnpackagerBlockEntity.TYPE_INSTANCE).ifPresent(UnpackagerBlockEntity::updatePowered);
	}
}
