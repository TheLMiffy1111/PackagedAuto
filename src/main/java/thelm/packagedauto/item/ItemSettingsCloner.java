package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.api.ISettingsCloneable;
import thelm.packagedauto.api.ISettingsClonerItem;
import thelm.packagedauto.api.SettingsClonerData;
import thelm.packagedauto.client.IModelRegister;

public class ItemSettingsCloner extends Item implements ISettingsClonerItem, IModelRegister {

	public static final ItemSettingsCloner INSTANCE = new ItemSettingsCloner();
	public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation("packagedauto:settings_cloner#inventory");
	public static final ModelResourceLocation MODEL_LOCATION_FILLED = new ModelResourceLocation("packagedauto:settings_cloner_filled#inventory");

	protected ItemSettingsCloner() {
		setTranslationKey("packagedauto.settings_cloner");
		setRegistryName("packagedauto:settings_cloner");
		setCreativeTab(PackagedAuto.CREATIVE_TAB);
		setMaxStackSize(1);
	}

	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		if(!world.isRemote && !player.isSneaking()) {
			TileEntity tile = world.getTileEntity(pos);
			if(tile instanceof ISettingsCloneable) {
				ISettingsCloneable settable = (ISettingsCloneable)tile;
				String configName = settable.getConfigTypeName();
				ItemStack stack = player.getHeldItem(hand);
				SettingsClonerData data = getData(stack);
				if(data != null) {
					if(configName.equals(data.type()) && settable.loadConfig(data.data(), player)) {
						player.sendMessage(new TextComponentTranslation("item.packagedauto.settings_cloner.loaded"));
					}
					else {
						player.sendMessage(new TextComponentTranslation("item.packagedauto.settings_cloner.not_loaded").setStyle(new Style().setColor(TextFormatting.RED)));
					}
				}
				else {
					NBTTagCompound dataTag = new NBTTagCompound();
					if(settable.saveConfig(dataTag, player)) {
						if(!stack.hasTagCompound()) {
							stack.setTagCompound(new NBTTagCompound());
						}
						NBTTagCompound tag = stack.getTagCompound();
						tag.setString("Type", configName);
						tag.setTag("Data", dataTag);
						tag.setInteger("Dimension", world.provider.getDimension());
						tag.setIntArray("Position", new int[] {pos.getX(), pos.getY(), pos.getZ()});
						player.sendMessage(new TextComponentTranslation("item.packagedauto.settings_cloner.saved"));
					}
					else {
						player.sendMessage(new TextComponentTranslation("item.packagedauto.settings_cloner.not_saved").setStyle(new Style().setColor(TextFormatting.RED)));
					}
				}
				return EnumActionResult.SUCCESS;
			}
		}
		return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if(!worldIn.isRemote && playerIn.isSneaking()) {
			ItemStack stack = playerIn.getHeldItem(handIn).copy();
			if(getData(stack) != null) {
				playerIn.sendMessage(new TextComponentTranslation("item.packagedauto.settings_cloner.cleared"));
			}
			if(stack.hasTagCompound()) {
				NBTTagCompound nbt = stack.getTagCompound();
				nbt.removeTag("Type");
				nbt.removeTag("Data");
				nbt.removeTag("Dimension");
				nbt.removeTag("Position");
				if(nbt.isEmpty()) {
					stack.setTagCompound(null);
				}
			}
			return new ActionResult<>(EnumActionResult.SUCCESS, stack);
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		SettingsClonerData data = getData(stack);
		if(data != null) {
			String typeString = I18n.translateToLocal(data.type());
			tooltip.add(I18n.translateToLocalFormatted("item.packagedauto.settings_cloner.contents", typeString));
			tooltip.add(I18n.translateToLocalFormatted("misc.packagedauto.dimension", data.dimension()));
			String posString = "["+data.x()+", "+data.y()+", "+data.z()+"]";
			tooltip.add(I18n.translateToLocalFormatted("misc.packagedauto.position", posString));
		}
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}

	@Override
	public SettingsClonerData getData(ItemStack stack) {
		if(isFilled(stack)) {
			NBTTagCompound nbt = stack.getTagCompound();
			String type = nbt.getString("Type");
			NBTTagCompound data = nbt.getCompoundTag("Data");
			int dimension = nbt.getInteger("Dimension");
			int[] posArray = nbt.getIntArray("Position");
			BlockPos blockPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
			return new SettingsClonerData(type, data, dimension, blockPos);
		}
		return null;
	}

	public boolean isFilled(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		return nbt != null && nbt.hasKey("Type") && nbt.hasKey("Data") && nbt.hasKey("Dimension") && nbt.hasKey("Position");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels() {
		ModelLoader.setCustomMeshDefinition(this, stack->isFilled(stack) ? MODEL_LOCATION_FILLED : MODEL_LOCATION);
		ModelBakery.registerItemVariants(this, MODEL_LOCATION, MODEL_LOCATION_FILLED);
	}
}
