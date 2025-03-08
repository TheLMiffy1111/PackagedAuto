package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IMarkerItem;
import thelm.packagedauto.client.IModelRegister;

public abstract class ItemMarker extends Item implements IMarkerItem, IModelRegister {

	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		if(!world.isRemote && !player.isSneaking()) {
			ItemStack stack = player.getHeldItem(hand);
			if(getDirectionalGlobalPos(stack) != null) {
				return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
			}
			int dim = world.provider.getDimension();
			DirectionalGlobalPos globalPos = new DirectionalGlobalPos(dim, pos, side);
			if(stack.getCount() > 1) {
				ItemStack stack1 = stack.splitStack(1);
				setDirectionalGlobalPos(stack1, globalPos);
				if(!player.inventory.addItemStackToInventory(stack1)) {
					EntityItem item = new EntityItem(world, player.posX, player.posY, player.posZ, stack1);
					item.setThrower(player.getName());
					world.spawnEntity(item);
				}
			}
			else {
				setDirectionalGlobalPos(stack, globalPos);
			}
			return EnumActionResult.SUCCESS;
		}
		return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if(!worldIn.isRemote && playerIn.isSneaking() && isBound(playerIn.getHeldItem(handIn))) {
			ItemStack stack = playerIn.getHeldItem(handIn).copy();
			setDirectionalGlobalPos(stack, null);
			return new ActionResult<>(EnumActionResult.SUCCESS, stack);
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		DirectionalGlobalPos pos = getDirectionalGlobalPos(stack);
		if(pos != null) {
			tooltip.add(I18n.translateToLocalFormatted("misc.packagedauto.dimension", pos.dimension()));
			String posString = "["+pos.x()+", "+pos.y()+", "+pos.z()+"]";
			tooltip.add(I18n.translateToLocalFormatted("misc.packagedauto.position", posString));
			String dirString = I18n.translateToLocal("misc.packagedauto."+pos.direction().getName());
			tooltip.add(I18n.translateToLocalFormatted("misc.packagedauto.direction", dirString));
		}
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}

	@Override
	public DirectionalGlobalPos getDirectionalGlobalPos(ItemStack stack) {
		if(isBound(stack)) {
			NBTTagCompound nbt = stack.getTagCompound();
			int dimension = nbt.getInteger("Dimension");
			int[] posArray = nbt.getIntArray("Position");
			BlockPos blockPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
			EnumFacing direction = EnumFacing.byIndex(nbt.getByte("Direction"));
			return new DirectionalGlobalPos(dimension, blockPos, direction);
		}
		return null;
	}

	@Override
	public void setDirectionalGlobalPos(ItemStack stack, DirectionalGlobalPos pos) {
		if(pos != null) {
			if(!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			NBTTagCompound nbt = stack.getTagCompound();
			nbt.setInteger("Dimension", pos.dimension());
			nbt.setIntArray("Position", new int[] {pos.x(), pos.y(), pos.z()});
			nbt.setByte("Direction", (byte)pos.direction().getIndex());
		}
		else if(stack.hasTagCompound()) {
			NBTTagCompound nbt = stack.getTagCompound();
			nbt.removeTag("Dimension");
			nbt.removeTag("Position");
			nbt.removeTag("Direction");
			if(nbt.isEmpty()) {
				stack.setTagCompound(null);
			}
		}
	}

	public boolean isBound(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		return nbt != null && nbt.hasKey("Dimension") && nbt.hasKey("Position") && nbt.hasKey("Direction");
	}
}
