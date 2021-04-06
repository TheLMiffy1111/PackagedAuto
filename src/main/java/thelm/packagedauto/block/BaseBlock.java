package thelm.packagedauto.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import thelm.packagedauto.tile.BaseTile;

public abstract class BaseBlock extends Block {

	protected BaseBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param) {
		super.eventReceived(state, worldIn, pos, id, param);
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult rayTraceResult) {
		if(playerIn.isSneaking()) {
			return ActionResultType.PASS;
		}
		if(!worldIn.isRemote) {
			TileEntity tile = worldIn.getTileEntity(pos);
			if(tile instanceof INamedContainerProvider) {
				NetworkHooks.openGui((ServerPlayerEntity)playerIn, (INamedContainerProvider)tile, pos);
			}
		}
		return ActionResultType.SUCCESS;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if(!worldIn.isRemote) {
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if(tileentity instanceof BaseTile) {
				if(stack.hasDisplayName()) {
					((BaseTile)tileentity).setCustomName(stack.getDisplayName());
				}
				if(placer instanceof PlayerEntity) {
					((BaseTile)tileentity).setPlacer((PlayerEntity)placer);
				}
			}
		}
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if(tileentity instanceof BaseTile) {
			IItemHandler handler = ((BaseTile)tileentity).getItemHandler();
			for(int i = 0; i < handler.getSlots(); ++i) {
				ItemStack stack = handler.getStackInSlot(i);
				if(!stack.isEmpty()) {
					InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
				}
			}
		}
		super.onReplaced(state, worldIn, pos, newState, isMoving);
	}

	@Override
	public boolean hasComparatorInputOverride(BlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if(tileentity instanceof BaseTile) {
			return ItemHandlerHelper.calcRedstoneFromInventory(((BaseTile)tileentity).getItemHandler());
		}
		return 0;
	}
}
