package thelm.packagedauto.volume;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.api.IVolumeType;

public class UnknownStackWrapper implements IVolumeStackWrapper {

	public static final UnknownStackWrapper INSTANCE = new UnknownStackWrapper();

	@Override
	public IVolumeType getVolumeType() {
		return null;
	}

	@Override
	public int getAmount() {
		return 0;
	}

	@Override
	public IVolumeStackWrapper copy() {
		return INSTANCE;
	}

	@Override
	public void setAmount(int amount) {

	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		return tag;
	}

	@Override
	public CompoundTag saveAEKey(CompoundTag tag) {
		return tag;
	}

	@Override
	public Component getDisplayName() {
		return new TextComponent("Unknown");
	}

	@Override
	public Component getAmountDesc() {
		return new TextComponent("");
	}

	@Override
	public List<Component> getTooltip() {
		return Lists.newArrayList(new TextComponent("Unknown"));
	}
}
