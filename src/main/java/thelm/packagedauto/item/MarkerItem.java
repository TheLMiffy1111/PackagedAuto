package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IMarkerItem;

public abstract class MarkerItem extends Item implements IMarkerItem {

	public MarkerItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
		Level level = context.getLevel();
		Player player = context.getPlayer();
		if(!level.isClientSide && !player.isShiftKeyDown()) {
			if(getDirectionalGlobalPos(stack) != null) {
				return super.onItemUseFirst(stack, context);
			}
			ResourceKey<Level> dim = level.dimension();
			BlockPos pos = context.getClickedPos();
			Direction dir = context.getClickedFace();
			DirectionalGlobalPos globalPos = new DirectionalGlobalPos(dim, pos, dir);
			if(stack.getCount() > 1) {
				ItemStack stack1 = stack.split(1);
				setDirectionalGlobalPos(stack1, globalPos);
				if(!player.getInventory().add(stack1)) {
					ItemEntity item = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack1);
					item.setThrower(player.getUUID());
					level.addFreshEntity(item);
				}
			}
			else {
				setDirectionalGlobalPos(stack, globalPos);
			}
			return InteractionResult.SUCCESS;
		}
		return super.onItemUseFirst(stack, context);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if(!level.isClientSide && player.isShiftKeyDown() && isBound(player.getItemInHand(hand))) {
			ItemStack stack = player.getItemInHand(hand).copy();
			setDirectionalGlobalPos(stack, null);
			return InteractionResultHolder.success(stack);
		}
		return super.use(level, player, hand);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
		DirectionalGlobalPos pos = getDirectionalGlobalPos(stack);
		if(pos != null) {
			Component dimComponent = Component.literal(pos.dimension().location().toString());
			tooltip.add(Component.translatable("misc.packagedauto.dimension", dimComponent));
			Component posComponent = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", pos.x(), pos.y(), pos.z()));
			tooltip.add(Component.translatable("misc.packagedauto.position", posComponent));
			Component dirComponent = Component.translatable("misc.packagedauto."+pos.direction().getName());
			tooltip.add(Component.translatable("misc.packagedauto.direction", dirComponent));
		}
		super.appendHoverText(stack, level, tooltip, isAdvanced);
	}

	@Override
	public DirectionalGlobalPos getDirectionalGlobalPos(ItemStack stack) {
		if(isBound(stack)) {
			CompoundTag nbt = stack.getTag();
			ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(nbt.getString("Dimension")));
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
			CompoundTag nbt = stack.getOrCreateTag();
			nbt.putString("Dimension", pos.dimension().location().toString());
			nbt.putIntArray("Position", new int[] {pos.x(), pos.y(), pos.z()});
			nbt.putByte("Direction", (byte)pos.direction().get3DDataValue());
		}
		else if(stack.hasTag()) {
			CompoundTag nbt = stack.getTag();
			nbt.remove("Dimension");
			nbt.remove("Position");
			nbt.remove("Direction");
			if(nbt.isEmpty()) {
				stack.setTag(null);
			}
		}
	}

	public boolean isBound(ItemStack stack) {
		CompoundTag nbt = stack.getTag();
		return nbt != null && nbt.contains("Dimension") && nbt.contains("Position") && nbt.contains("Direction");
	}
}
