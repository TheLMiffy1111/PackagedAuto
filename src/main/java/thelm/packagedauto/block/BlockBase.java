package thelm.packagedauto.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.client.IModelRegister;
import thelm.packagedauto.tile.TileBase;

public abstract class BlockBase extends Block implements ITileEntityProvider, IModelRegister {

	public BlockBase(Material material) {
		super(material);
	}

	public BlockBase(Material material, MapColor mapColor) {
		super(material, mapColor);
	}

	@Override
	public abstract TileBase createNewTileEntity(World worldIn, int meta);

	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
		if(te instanceof TileBase && ((TileBase)te).hasCustomName()) {
			player.addStat(StatList.getBlockStats(this));
			player.addExhaustion(0.005F);
			if(worldIn.isRemote) {
				return;
			}
			int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
			Item item = getItemDropped(state, worldIn.rand, i);
			if(item == Items.AIR) {
				return;
			}
			ItemStack itemstack = new ItemStack(item, this.quantityDropped(worldIn.rand));
			itemstack.setStackDisplayName(((IWorldNameable)te).getName());
			spawnAsEntity(worldIn, pos, itemstack);
		}
		else {
			super.harvestBlock(worldIn, player, pos, state, te, stack);
		}
	}

	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
		super.eventReceived(state, worldIn, pos, id, param);
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(playerIn.isSneaking()) {
			return false;
		}
		if(!worldIn.isRemote) {
			playerIn.openGui(PackagedAuto.instance, facing.getIndex(), worldIn, pos.getX(), pos.getY(), pos.getZ());
			return true;
		}
		return true;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		if(!worldIn.isRemote) {
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if(tileentity instanceof TileBase) {
				if(stack.hasDisplayName()) {
					((TileBase)tileentity).setCustomName(stack.getDisplayName());
				}
				if(placer instanceof EntityPlayer) {
					((TileBase)tileentity).setPlacer((EntityPlayer)placer);
				}
			}
		}
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state)  {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if(tileentity instanceof TileBase) {
			InventoryHelper.dropInventoryItems(worldIn, pos, ((TileBase)tileentity).getInventory());
			worldIn.updateComparatorOutputLevel(pos, this);
		}
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if(tileentity instanceof TileBase) {
			return Container.calcRedstoneFromInventory(((TileBase)tileentity).getInventory());
		}
		return 0;
	}
}
