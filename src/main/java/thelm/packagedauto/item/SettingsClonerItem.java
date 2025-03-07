package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import thelm.packagedauto.api.ISettingsCloneable;
import thelm.packagedauto.api.SettingsClonerData;
import thelm.packagedauto.component.PackagedAutoDataComponents;

public class SettingsClonerItem extends Item {

	protected SettingsClonerItem() {
		super(new Item.Properties().stacksTo(1));
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
		Level level = context.getLevel();
		Player player = context.getPlayer();
		if(!level.isClientSide && !player.isShiftKeyDown()) {
			BlockPos pos = context.getClickedPos();
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof ISettingsCloneable settable) {
				String configName = settable.getConfigTypeName();
				if(stack.has(PackagedAutoDataComponents.CLONER_DATA)) {
					SettingsClonerData data = stack.get(PackagedAutoDataComponents.CLONER_DATA);
					if(configName.equals(data.type()) && settable.loadConfig(data.data(), level.registryAccess(), player)) {
						player.sendSystemMessage(Component.translatable("item.packagedauto.settings_cloner.loaded"));
					}
					else {
						player.sendSystemMessage(Component.translatable("item.packagedauto.settings_cloner.not_loaded").withStyle(ChatFormatting.RED));
					}
				}
				else {
					CompoundTag dataTag = new CompoundTag();
					if(settable.saveConfig(dataTag, level.registryAccess(), player)) {
						SettingsClonerData data = new SettingsClonerData(configName, dataTag, level.dimension(), pos);
						DataComponentPatch patch = DataComponentPatch.builder().
								set(PackagedAutoDataComponents.CLONER_DATA.get(), data).
								build();
						stack.applyComponents(patch);
						player.sendSystemMessage(Component.translatable("item.packagedauto.settings_cloner.saved"));
					}
					else {
						player.sendSystemMessage(Component.translatable("item.packagedauto.settings_cloner.not_saved").withStyle(ChatFormatting.RED));
					}
				}
				return InteractionResult.SUCCESS;
			}
		}
		return super.onItemUseFirst(stack, context);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if(!level.isClientSide && player.isShiftKeyDown()) {
			ItemStack stack = player.getItemInHand(hand).copy();
			if(stack.has(PackagedAutoDataComponents.CLONER_DATA)) {
				player.sendSystemMessage(Component.translatable("item.packagedauto.settings_cloner.cleared"));
				DataComponentPatch patch = DataComponentPatch.builder().
						remove(PackagedAutoDataComponents.CLONER_DATA.get()).
						build();
				stack.applyComponents(patch);
				return InteractionResultHolder.success(stack);
			}
		}
		return super.use(level, player, hand);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag isAdvanced) {
		if(stack.has(PackagedAutoDataComponents.CLONER_DATA)) {
			SettingsClonerData data = stack.get(PackagedAutoDataComponents.CLONER_DATA);
			Component typeComponent = Component.translatable(data.type());
			tooltip.add(Component.translatable("item.packagedauto.settings_cloner.contents", typeComponent));
			Component dimComponent = Component.literal(data.dimension().location().toString());
			tooltip.add(Component.translatable("item.packagedauto.settings_cloner.dimension", dimComponent));
			Component posComponent = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", data.x(), data.y(), data.z()));
			tooltip.add(Component.translatable("item.packagedauto.settings_cloner.position", posComponent));
		}
		super.appendHoverText(stack, context, tooltip, isAdvanced);
	}
}
