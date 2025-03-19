package thelm.packagedauto.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;
import thelm.packagedauto.block.entity.BaseBlockEntity;

public abstract class BaseBlock extends Block implements EntityBlock {

	protected BaseBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		super.triggerEvent(state, level, pos, id, param);
		BlockEntity blockentity = level.getBlockEntity(pos);
		return blockentity == null ? false : blockentity.triggerEvent(id, param);
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if(player.isShiftKeyDown()) {
			return InteractionResult.PASS;
		}
		if(!level.isClientSide) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof MenuProvider menuProvider) {
				NetworkHooks.openScreen((ServerPlayer)player, menuProvider, pos);
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if(!level.isClientSide) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof BaseBlockEntity baseBlockEntity) {
				if(stack.hasCustomHoverName()) {
					baseBlockEntity.setCustomName(stack.getHoverName());
				}
				if(placer instanceof Player player) {
					baseBlockEntity.setOwner(player);
				}
			}
		}
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if(state.getBlock() != newState.getBlock()) {
			if(level.getBlockEntity(pos) instanceof BaseBlockEntity blockEntity) {
				IItemHandler handler = blockEntity.getItemHandler();
				for(int i = 0; i < handler.getSlots(); ++i) {
					ItemStack stack = handler.getStackInSlot(i);
					if(!stack.isEmpty()) {
						Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
					}
				}
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState pState, Level level, BlockPos pos) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof BaseBlockEntity baseBlockEntity) {
			return baseBlockEntity.getComparatorSignal();
		}
		return 0;
	}
}
