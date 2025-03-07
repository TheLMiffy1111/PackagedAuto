package thelm.packagedauto.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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
import thelm.packagedauto.block.entity.PackagingProviderBlockEntity;

public class PackagingProviderBlock extends BaseBlock {

	public static final PackagingProviderBlock INSTANCE = new PackagingProviderBlock();
	public static final Item ITEM_INSTANCE = new BlockItem(INSTANCE, new Item.Properties());

	protected PackagingProviderBlock() {
		super(BlockBehaviour.Properties.of().strength(15F, 25F).mapColor(MapColor.METAL).sound(SoundType.METAL));
	}

	@Override
	public PackagingProviderBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return PackagingProviderBlockEntity.TYPE_INSTANCE.create(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return BaseBlockEntity::tick;
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if(state.getBlock() != newState.getBlock()) {
			if(level.getBlockEntity(pos) instanceof PackagingProviderBlockEntity provider) {
				if(provider.currentPattern != null) {
					for(ItemStack stack : provider.currentPattern.getInputs()) {
						if(!stack.isEmpty()) {
							Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
						}
					}
				}
				if(!provider.toSend.isEmpty()) {
					for(ItemStack stack : provider.toSend) {
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
		level.getBlockEntity(pos, PackagingProviderBlockEntity.TYPE_INSTANCE).ifPresent(PackagingProviderBlockEntity::updatePowered);
	}
}
