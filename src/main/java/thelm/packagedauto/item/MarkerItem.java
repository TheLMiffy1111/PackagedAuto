package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IMarkerItem;

public abstract class MarkerItem extends Item implements IMarkerItem {

	public MarkerItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
		World world = context.getLevel();
		PlayerEntity player = context.getPlayer();
		if(!world.isClientSide && !player.isShiftKeyDown()) {
			if(getDirectionalGlobalPos(stack) != null) {
				return super.onItemUseFirst(stack, context);
			}
			RegistryKey<World> dim = world.dimension();
			BlockPos pos = context.getClickedPos();
			Direction dir = context.getClickedFace();
			DirectionalGlobalPos globalPos = new DirectionalGlobalPos(dim, pos, dir);
			if(stack.getCount() > 1) {
				ItemStack stack1 = stack.split(1);
				setDirectionalGlobalPos(stack1, globalPos);
				if(!player.inventory.add(stack1)) {
					ItemEntity item = new ItemEntity(world, player.getX(), player.getY(), player.getZ(), stack1);
					item.setThrower(player.getUUID());
					world.addFreshEntity(item);
				}
			}
			else {
				setDirectionalGlobalPos(stack, globalPos);
			}
			return ActionResultType.SUCCESS;
		}
		return super.onItemUseFirst(stack, context);
	}

	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if(!worldIn.isClientSide && playerIn.isShiftKeyDown() && isBound(playerIn.getItemInHand(handIn))) {
			ItemStack stack = playerIn.getItemInHand(handIn).copy();
			setDirectionalGlobalPos(stack, null);
			return ActionResult.success(stack);
		}
		return super.use(worldIn, playerIn, handIn);
	}

	@Override
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		DirectionalGlobalPos pos = getDirectionalGlobalPos(stack);
		if(pos != null) {
			ITextComponent dimComponent = new StringTextComponent(pos.dimension().location().toString());
			tooltip.add(new TranslationTextComponent("misc.packagedauto.dimension", dimComponent));
			ITextComponent posComponent = TextComponentUtils.wrapInSquareBrackets(new TranslationTextComponent("chat.coordinates", pos.x(), pos.y(), pos.z()));
			tooltip.add(new TranslationTextComponent("misc.packagedauto.position", posComponent));
			ITextComponent dirComponent = new TranslationTextComponent("misc.packagedauto."+pos.direction().getName());
			tooltip.add(new TranslationTextComponent("misc.packagedauto.direction", dirComponent));
		}
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
	}

	@Override
	public DirectionalGlobalPos getDirectionalGlobalPos(ItemStack stack) {
		if(isBound(stack)) {
			CompoundNBT nbt = stack.getTag();
			RegistryKey<World> dimension = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(nbt.getString("Dimension")));
			int[] posArray = nbt.getIntArray("Position");
			BlockPos blockPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
			Direction direction = Direction.from3DDataValue(nbt.getByte("Direction"));
			return new DirectionalGlobalPos(dimension, blockPos, direction);
		}
		return null;
	}

	@Override
	public void setDirectionalGlobalPos(ItemStack stack, DirectionalGlobalPos pos) {
		if(pos != null) {
			CompoundNBT nbt = stack.getOrCreateTag();
			nbt.putString("Dimension", pos.dimension().location().toString());
			nbt.putIntArray("Position", new int[] {pos.x(), pos.y(), pos.z()});
			nbt.putByte("Direction", (byte)pos.direction().get3DDataValue());
		}
		else if(stack.hasTag()) {
			CompoundNBT nbt = stack.getTag();
			nbt.remove("Dimension");
			nbt.remove("Position");
			nbt.remove("Direction");
			if(nbt.isEmpty()) {
				stack.setTag(null);
			}
		}
	}

	public boolean isBound(ItemStack stack) {
		CompoundNBT nbt = stack.getTag();
		return nbt != null && nbt.contains("Dimension") && nbt.contains("Position") && nbt.contains("Direction");
	}
}
