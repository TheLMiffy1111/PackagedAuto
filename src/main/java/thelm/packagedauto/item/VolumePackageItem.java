package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import thelm.packagedauto.api.IVolumePackageItem;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.api.IVolumeType;
import thelm.packagedauto.util.ApiImpl;
import thelm.packagedauto.volume.UnknownStackWrapper;

public class VolumePackageItem extends Item implements IVolumePackageItem {

	public static final VolumePackageItem INSTANCE = new VolumePackageItem();

	protected VolumePackageItem() {
		super(new Item.Properties());
	}

	public static ItemStack makeVolumePackage(IVolumeStackWrapper volumeStack) {
		if(volumeStack.isEmpty()) {
			return ItemStack.EMPTY;
		}	
		IVolumeType type = volumeStack.getVolumeType();
		ItemStack stack = new ItemStack(INSTANCE);
		CompoundTag nbt = new CompoundTag();
		nbt.putString("Type", type.getName().toString());
		stack.setTag(nbt);
		type.setStack(stack, volumeStack);
		return stack;
	}

	public static ItemStack tryMakeVolumePackage(Object volumeStack) {
		if(volumeStack == null) {
			return ItemStack.EMPTY;
		}
		IVolumeType type = ApiImpl.INSTANCE.getVolumeType(volumeStack.getClass());
		if(type == null) {
			return ItemStack.EMPTY;
		}
		return type.wrapStack(volumeStack).map(s->makeVolumePackage(s)).orElse(ItemStack.EMPTY);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
		IVolumeStackWrapper volumeStack = getVolumeStack(stack);
		if(!volumeStack.isEmpty()) {
			tooltip.add(volumeStack.getVolumeType().getDisplayName().append(": ").
					append(volumeStack.getDisplayName()).append(" ").
					append(volumeStack.getAmountDesc()));
		}
		super.appendHoverText(stack, level, tooltip, isAdvanced);
	}

	@Override
	public IVolumeType getVolumeType(ItemStack stack) {
		CompoundTag nbt = stack.getTag();
		if(nbt == null || !nbt.contains("Type")) {
			return null;
		}
		return ApiImpl.INSTANCE.getVolumeType(new ResourceLocation(nbt.getString("Type")));
	}

	@Override
	public IVolumeStackWrapper getVolumeStack(ItemStack stack) {
		IVolumeType type = getVolumeType(stack);
		if(type != null) {
			return type.getStackContained(stack).orElse(type.getEmptyStackInstance());
		}
		return UnknownStackWrapper.INSTANCE;
	}
}
