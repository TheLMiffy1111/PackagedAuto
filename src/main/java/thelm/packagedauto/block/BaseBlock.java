package thelm.packagedauto.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import thelm.packagedauto.block.entity.BaseBlockEntity;

public abstract class BaseBlock extends Block implements EntityBlock {

	protected BaseBlock(Properties properties) {
		super(properties);
	}

	@Override
	public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		super.triggerEvent(state, level, pos, id, param);
		BlockEntity blockentity = level.getBlockEntity(pos);
		return blockentity == null ? false : blockentity.triggerEvent(id, param);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if(player.isShiftKeyDown()) {
			return InteractionResult.PASS;
		}
		if(!level.isClientSide()) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof MenuProvider menuProvider) {
				player.openMenu(menuProvider, buf->buf.writeBlockPos(pos));
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if(!level.isClientSide()) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof BaseBlockEntity baseBlockEntity) {
				if(placer instanceof Player player) {
					baseBlockEntity.setOwner(player);
				}
			}
		}
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof BaseBlockEntity baseBlockEntity) {
			return baseBlockEntity.getComparatorSignal();
		}
		return 0;
	}
}
