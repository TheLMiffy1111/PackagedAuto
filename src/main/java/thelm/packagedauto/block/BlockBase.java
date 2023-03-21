package thelm.packagedauto.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.tile.TileBase;

public abstract class BlockBase extends Block implements ITileEntityProvider {

	protected final Random rand = new Random();

	public BlockBase(Material material) {
		super(material);
	}

	@Override
	public abstract TileBase createNewTileEntity(World worldIn, int meta);

	@Override
	public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta) {
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileBase && ((TileBase)te).hasCustomInventoryName()) {
			player.addStat(StatList.mineBlockStatArray[getIdFromBlock(this)], 1);
			player.addExhaustion(0.005F);
			if(world.isRemote) {
				return;
			}
			int i = EnchantmentHelper.getFortuneModifier(player);
			Item item = getItemDropped(meta, world.rand, i);
			if(item == null) {
				return;
			}
			ItemStack itemstack = new ItemStack(item, quantityDropped(world.rand));
			itemstack.setStackDisplayName(((TileBase)te).getInventoryName());
			dropBlockAsItem(world, x, y, z, itemstack);
		}
		else {
			super.harvestBlock(world, player, x, y, z, meta);
		}
	}

	@Override
	public boolean onBlockEventReceived(World world, int x, int y, int z, int id, int param) {
		super.onBlockEventReceived(world, x, y, z, id, param);
		TileEntity tileentity = world.getTileEntity(x, y, z);
		return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float subX, float subY, float subZ) {
		if(player.isSneaking()) {
			return false;
		}
		if(!world.isRemote) {
			player.openGui(PackagedAuto.instance, side, world, x, y, z);
			return true;
		}
		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(world, x, y, z, placer, stack);
		if(!world.isRemote) {
			TileEntity tileentity = world.getTileEntity(x, y, z);
			if(tileentity instanceof TileBase) {
				if(stack.hasDisplayName()) {
					((TileBase)tileentity).setCustomName(stack.getDisplayName());
				}
				if(placer instanceof EntityPlayer) {
					((TileBase)tileentity).setOwner((EntityPlayer)placer);
				}
			}
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		TileEntity tileentity = world.getTileEntity(x, y, z);
		if(tileentity instanceof TileBase) {
			TileBase te = (TileBase)tileentity;
			for(int i = 0; i < te.getSizeInventory(); ++i)  {
				spawnItemStack(world, x, y, z, te.getStackInSlot(i));
			}
			world.func_147453_f(x, y, z, block);
		}
		super.breakBlock(world, x, y, z, block, meta);
	}

	@Override
	public boolean hasComparatorInputOverride() {
		return true;
	}

	@Override
	public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
		TileEntity tileentity = world.getTileEntity(x, y, z);
		if(tileentity instanceof TileBase) {
			return Container.calcRedstoneFromInventory((TileBase)tileentity);
		}
		return 0;
	}

	public void spawnItemStack(World world, double x, double y, double z, ItemStack stack) {
		if(stack != null) {
			float fx = rand.nextFloat()*0.8F+0.1F;
			float fy = rand.nextFloat()*0.8F+0.1F;
			float fz = rand.nextFloat()*0.8F+0.1F;
			while(stack.stackSize > 0) {
				EntityItem entityitem = new EntityItem(world, x+fx, y+fy, z+fz, stack.splitStack(Math.min(rand.nextInt(21)+10, stack.stackSize)));
				entityitem.motionX = rand.nextGaussian()*0.05;
				entityitem.motionY = rand.nextGaussian()*0.05+0.2;
				entityitem.motionZ = rand.nextGaussian()*0.05;
				world.spawnEntityInWorld(entityitem);
			}
		}
	}
}
