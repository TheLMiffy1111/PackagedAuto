package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.api.ISettingsCloneable;
import thelm.packagedauto.api.ISettingsClonerItem;
import thelm.packagedauto.api.SettingsClonerData;

public class SettingsClonerItem extends Item implements ISettingsClonerItem {

	public static final SettingsClonerItem INSTANCE = new SettingsClonerItem();

	protected SettingsClonerItem() {
		super(new Item.Properties().stacksTo(1).tab(PackagedAuto.ITEM_GROUP));
		setRegistryName("packagedauto:settings_cloner");
	}

	@Override
	public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
		World world = context.getLevel();
		PlayerEntity player = context.getPlayer();
		BlockPos pos = context.getClickedPos();
		TileEntity tile = world.getBlockEntity(pos);
		if(tile instanceof ISettingsCloneable) {
			ISettingsCloneable settable = (ISettingsCloneable)tile;
			String configName = settable.getConfigTypeName();
			if(player.isShiftKeyDown()) {
				if(!world.isClientSide) {
					CompoundNBT dataTag = new CompoundNBT();
					ISettingsCloneable.Result result = settable.saveConfig(dataTag, player);
					if(result.type != ISettingsCloneable.ResultType.FAIL) {
						CompoundNBT tag = stack.getOrCreateTag();
						tag.putString("Type", configName);
						tag.put("Data", dataTag);
						tag.putString("Dimension", world.dimension().location().toString());
						tag.putIntArray("Position", new int[] {pos.getX(), pos.getY(), pos.getZ()});
						player.sendMessage(new TranslationTextComponent("item.packagedauto.settings_cloner.saved"), Util.NIL_UUID);
					}
					else {
						player.sendMessage(new TranslationTextComponent("item.packagedauto.settings_cloner.not_saved", result.message).withStyle(TextFormatting.RED), Util.NIL_UUID);
					}
				}
				return ActionResultType.SUCCESS;
			}
			SettingsClonerData data = getData(stack);
			if(data != null) {
				if(!world.isClientSide) {
					if(configName.equals(data.type())) {
						ISettingsCloneable.Result result = settable.loadConfig(data.data(), player);
						switch(result.type) {
						case SUCCESS:
							player.sendMessage(new TranslationTextComponent("item.packagedauto.settings_cloner.loaded"), Util.NIL_UUID);
							break;
						case PARTIAL:
							player.sendMessage(new TranslationTextComponent("item.packagedauto.settings_cloner.partial_loaded", result.message), Util.NIL_UUID);
							break;
						case FAIL:
							player.sendMessage(new TranslationTextComponent("item.packagedauto.settings_cloner.not_loaded", result.message).withStyle(TextFormatting.RED), Util.NIL_UUID);
							break;
						}
					}
					else {
						ITextComponent errorMessage = new TranslationTextComponent("item.packagedauto.settings_cloner.incompatible");
						player.sendMessage(new TranslationTextComponent("item.packagedauto.settings_cloner.not_loaded", errorMessage).withStyle(TextFormatting.RED), Util.NIL_UUID);
					}
				}
				return ActionResultType.SUCCESS;
			}
		}
		return super.onItemUseFirst(stack, context);
	}

	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if(!worldIn.isClientSide && playerIn.isShiftKeyDown() && hasData(playerIn.getItemInHand(handIn))) {
			ItemStack stack = playerIn.getItemInHand(handIn).copy();
			CompoundNBT nbt = stack.getTag();
			nbt.remove("Type");
			nbt.remove("Data");
			nbt.remove("Dimension");
			nbt.remove("Position");
			if(nbt.isEmpty()) {
				stack.setTag(null);
			}
			playerIn.sendMessage(new TranslationTextComponent("item.packagedauto.settings_cloner.cleared"), Util.NIL_UUID);
			return ActionResult.success(stack);
		}
		return super.use(worldIn, playerIn, handIn);
	}

	@Override
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		SettingsClonerData data = getData(stack);
		if(data != null) {
			ITextComponent typeComponent = new TranslationTextComponent(data.type());
			tooltip.add(new TranslationTextComponent("item.packagedauto.settings_cloner.contents", typeComponent));
			ITextComponent dimComponent = new StringTextComponent(data.dimension().location().toString());
			tooltip.add(new TranslationTextComponent("misc.packagedauto.dimension", dimComponent));
			ITextComponent posComponent = TextComponentUtils.wrapInSquareBrackets(new TranslationTextComponent("chat.coordinates", data.x(), data.y(), data.z()));
			tooltip.add(new TranslationTextComponent("misc.packagedauto.position", posComponent));
		}
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
	}

	@Override
	public SettingsClonerData getData(ItemStack stack) {
		if(hasData(stack)) {
			CompoundNBT nbt = stack.getTag();
			String type = nbt.getString("Type");
			CompoundNBT data = nbt.getCompound("Data");
			RegistryKey<World> dimension = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(nbt.getString("Dimension")));
			int[] posArray = nbt.getIntArray("Position");
			BlockPos blockPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
			return new SettingsClonerData(type, data, dimension, blockPos);
		}
		return null;
	}

	public boolean hasData(ItemStack stack) {
		CompoundNBT nbt = stack.getTag();
		return nbt != null && nbt.contains("Type") && nbt.contains("Data") && nbt.contains("Dimension") && nbt.contains("Position");
	}
}
