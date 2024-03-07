package thelm.packagedauto.volume;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.api.IVolumeType;

public record FluidStackWrapper(FluidStack stack) implements IVolumeStackWrapper {

	public static final FluidStackWrapper EMPTY = new FluidStackWrapper(FluidStack.EMPTY);

	@Override
	public IVolumeType getVolumeType() {
		return FluidVolumeType.INSTANCE;
	}

	@Override
	public int getAmount() {
		return stack.getAmount();
	}

	@Override
	public IVolumeStackWrapper copy() {
		return new FluidStackWrapper(stack.copy());
	}

	@Override
	public void setAmount(int amount) {
		stack.setAmount(amount);
	}

	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		return stack.writeToNBT(tag);
	}

	@Override
	public CompoundTag saveAEKey(CompoundTag tag) {
		tag.putString("#c", "ae2:f");
		tag.putString("id", BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString());
		if(stack.hasTag()) {
			tag.put("tag", stack.getTag().copy());
		}
		return tag;
	}

	@Override
	public Component getDisplayName() {
		return stack.getDisplayName();
	}

	@Override
	public Component getAmountDesc() {
		return Component.literal(stack.getAmount()+"mB");
	}

	@Override
	public List<Component> getTooltip() {
		return Lists.newArrayList(stack.getDisplayName());
	}

	@Override
	public int hashCode() {
		return stack.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof FluidStackWrapper other) {
			return stack.equals(other.stack);
		}
		return false;
	}
}
