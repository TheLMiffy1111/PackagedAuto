package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
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
import thelm.packagedauto.component.PackagedAutoDataComponents;

public class MarkerItem extends Item {

	public MarkerItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
		Level level = context.getLevel();
		if(!level.isClientSide) {
			if(stack.has(PackagedAutoDataComponents.MARKER_POS)) {
				return super.onItemUseFirst(stack, context);
			}
			DirectionalGlobalPos pos = new DirectionalGlobalPos(level.dimension(), context.getClickedPos(), context.getClickedFace());
			DataComponentPatch patch = DataComponentPatch.builder().
					set(PackagedAutoDataComponents.MARKER_POS.get(), pos).
					build();
			if(stack.getCount() > 1) {
				ItemStack stack1 = stack.split(1);
				stack1.applyComponents(patch);
				Player player = context.getPlayer();
				if(!player.getInventory().add(stack1)) {
					ItemEntity item = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack1);
					item.setThrower(player);
					level.addFreshEntity(item);
				}
			}
			else {
				stack.applyComponents(patch);
			}
			return InteractionResult.SUCCESS;
		}
		return super.onItemUseFirst(stack, context);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if(!level.isClientSide && player.isShiftKeyDown()) {
			ItemStack stack = player.getItemInHand(hand).copy();
			DataComponentPatch patch = DataComponentPatch.builder().
					remove(PackagedAutoDataComponents.MARKER_POS.get()).
					build();
			stack.applyComponents(patch);
			return InteractionResultHolder.success(stack);
		}
		return super.use(level, player, hand);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag isAdvanced) {
		if(stack.has(PackagedAutoDataComponents.MARKER_POS)) {
			DirectionalGlobalPos pos = stack.get(PackagedAutoDataComponents.MARKER_POS);
			Component dimComponent = Component.literal(pos.dimension().location().toString());
			tooltip.add(Component.translatable("item.packagedauto.distributor_marker.dimension", dimComponent));
			Component posComponent = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", pos.x(), pos.y(), pos.z()));
			tooltip.add(Component.translatable("item.packagedauto.distributor_marker.position", posComponent));
			Component dirComponent = Component.translatable("misc.packagedauto."+pos.direction().getName());
			tooltip.add(Component.translatable("item.packagedauto.distributor_marker.direction", dirComponent));
		}
		super.appendHoverText(stack, context, tooltip, isAdvanced);
	}
}
