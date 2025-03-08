package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.api.ISettingsCloneable;
import thelm.packagedauto.api.ISettingsClonerItem;
import thelm.packagedauto.api.SettingsClonerData;

public class SettingsClonerItem extends Item implements ISettingsClonerItem {

	public static final SettingsClonerItem INSTANCE = new SettingsClonerItem();

	protected SettingsClonerItem() {
		super(new Item.Properties().stacksTo(1).tab(PackagedAuto.CREATIVE_TAB));
		setRegistryName("packagedauto:settings_cloner");
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
		Level level = context.getLevel();
		Player player = context.getPlayer();
		BlockPos pos = context.getClickedPos();
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof ISettingsCloneable settable) {
			String configName = settable.getConfigTypeName();
			if(player.isShiftKeyDown()) {
				if(!level.isClientSide) {
					CompoundTag dataTag = new CompoundTag();
					ISettingsCloneable.Result result = settable.saveConfig(dataTag, player);
					if(result.type() != ISettingsCloneable.ResultType.FAIL) {
						CompoundTag tag = stack.getOrCreateTag();
						tag.putString("Type", configName);
						tag.put("Data", dataTag);
						tag.putString("Dimension", level.dimension().location().toString());
						tag.putIntArray("Position", new int[] {pos.getX(), pos.getY(), pos.getZ()});
						player.sendMessage(new TranslatableComponent("item.packagedauto.settings_cloner.saved"), Util.NIL_UUID);
					}
					else {
						player.sendMessage(new TranslatableComponent("item.packagedauto.settings_cloner.not_saved", result.message()).withStyle(ChatFormatting.RED), Util.NIL_UUID);
					}
				}
				return InteractionResult.SUCCESS;
			}
			SettingsClonerData data = getData(stack);
			if(data != null) {
				if(!level.isClientSide) {
					if(configName.equals(data.type())) {
						ISettingsCloneable.Result result = settable.loadConfig(data.data(), player);
						switch(result.type()) {
						case SUCCESS -> player.sendMessage(new TranslatableComponent("item.packagedauto.settings_cloner.loaded"), Util.NIL_UUID);
						case PARTIAL -> player.sendMessage(new TranslatableComponent("item.packagedauto.settings_cloner.partial_loaded", result.message()), Util.NIL_UUID);
						case FAIL -> player.sendMessage(new TranslatableComponent("item.packagedauto.settings_cloner.not_loaded", result.message()).withStyle(ChatFormatting.RED), Util.NIL_UUID);
						}
					}
					else {
						Component errorMessage = new TranslatableComponent("item.packagedauto.settings_cloner.incompatible");
						player.sendMessage(new TranslatableComponent("item.packagedauto.settings_cloner.not_loaded", errorMessage).withStyle(ChatFormatting.RED), Util.NIL_UUID);
					}
				}
				return InteractionResult.SUCCESS;
			}
		}
		return super.onItemUseFirst(stack, context);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if(!level.isClientSide && player.isShiftKeyDown() && hasData(player.getItemInHand(hand))) {
			ItemStack stack = player.getItemInHand(hand).copy();
			CompoundTag nbt = stack.getTag();
			nbt.remove("Type");
			nbt.remove("Data");
			nbt.remove("Dimension");
			nbt.remove("Position");
			if(nbt.isEmpty()) {
				stack.setTag(null);
			}
			player.sendMessage(new TranslatableComponent("item.packagedauto.settings_cloner.cleared"), Util.NIL_UUID);
			return InteractionResultHolder.success(stack);
		}
		return super.use(level, player, hand);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
		SettingsClonerData data = getData(stack);
		if(data != null) {
			Component typeComponent = new TranslatableComponent(data.type());
			tooltip.add(new TranslatableComponent("item.packagedauto.settings_cloner.contents", typeComponent));
			Component dimComponent = new TextComponent(data.dimension().location().toString());
			tooltip.add(new TranslatableComponent("misc.packagedauto.dimension", dimComponent));
			Component posComponent = ComponentUtils.wrapInSquareBrackets(new TranslatableComponent("chat.coordinates", data.x(), data.y(), data.z()));
			tooltip.add(new TranslatableComponent("misc.packagedauto.position", posComponent));
		}
		super.appendHoverText(stack, level, tooltip, isAdvanced);
	}

	@Override
	public SettingsClonerData getData(ItemStack stack) {
		if(hasData(stack)) {
			CompoundTag nbt = stack.getTag();
			String type = nbt.getString("Type");
			CompoundTag data = nbt.getCompound("Data");
			ResourceKey<Level> dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(nbt.getString("Dimension")));
			int[] posArray = nbt.getIntArray("Position");
			BlockPos blockPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
			return new SettingsClonerData(type, data, dimension, blockPos);
		}
		return null;
	}

	public boolean hasData(ItemStack stack) {
		CompoundTag nbt = stack.getTag();
		return nbt != null && nbt.contains("Type") && nbt.contains("Data") && nbt.contains("Dimension") && nbt.contains("Position");
	}
}
