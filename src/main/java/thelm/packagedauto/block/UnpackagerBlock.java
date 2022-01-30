package thelm.packagedauto.block;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.block.entity.BaseBlockEntity;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity.PackageTracker;

public class UnpackagerBlock extends BaseBlock {

	public static final UnpackagerBlock INSTANCE = new UnpackagerBlock();
	public static final Item ITEM_INSTANCE = new BlockItem(INSTANCE, new Item.Properties().tab(PackagedAuto.CREATIVE_TAB)).setRegistryName("packagedauto:unpackager");

	protected UnpackagerBlock() {
		super(BlockBehaviour.Properties.of(Material.METAL).strength(15F, 25F).sound(SoundType.METAL));
		setRegistryName("packagedauto:unpackager");
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
	public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
		level.getBlockEntity(pos, UnpackagerBlockEntity.TYPE_INSTANCE).ifPresent(blockEntity->{
			for(PackageTracker tracker : blockEntity.trackers) {
				if(!tracker.isEmpty()) {
					if(!tracker.toSend.isEmpty()) {
						for(ItemStack stack : tracker.toSend) {
							if(!stack.isEmpty()) {
								Containers.dropItemStack((Level)level, pos.getX(), pos.getY(), pos.getZ(), stack);
							}
						}
					}
					else {
						List<IPackagePattern> patterns = tracker.recipe.getPatterns();
						for(int i = 0; i < tracker.received.size() && i < patterns.size(); ++i) {
							if(tracker.received.getBoolean(i)) {
								Containers.dropItemStack((Level)level, pos.getX(), pos.getY(), pos.getZ(), patterns.get(i).getOutput());
							}
						}
					}
				}
			}
		});
		super.destroy(level, pos, state);
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
		level.getBlockEntity(pos, UnpackagerBlockEntity.TYPE_INSTANCE).ifPresent(UnpackagerBlockEntity::updatePowered);
	}
}
