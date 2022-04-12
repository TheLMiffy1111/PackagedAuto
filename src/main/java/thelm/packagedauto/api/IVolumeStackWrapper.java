package thelm.packagedauto.api;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public interface IVolumeStackWrapper {

	IVolumeType getVolumeType();

	int getAmount();

	IVolumeStackWrapper copy();

	void setAmount(int amount);

	boolean isEmpty();

	CompoundTag save(CompoundTag tag);

	CompoundTag saveAEKey(CompoundTag tag);

	Component getDisplayName();

	Component getAmountDesc();

	List<Component> getTooltip();
}
