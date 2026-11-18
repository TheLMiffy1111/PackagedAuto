package thelm.packagedauto.api;

import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.ValueOutput;

public interface IVolumeStackWrapper {

	static final Codec<IVolumeStackWrapper> CODEC = IVolumeType.CODEC.dispatch(
			"type", IVolumeStackWrapper::getVolumeType, type->type.getStackCodec().fieldOf("stack"));
	static final StreamCodec<RegistryFriendlyByteBuf, IVolumeStackWrapper> STREAM_CODEC = IVolumeType.STREAM_CODEC.
			<RegistryFriendlyByteBuf>cast().dispatch(IVolumeStackWrapper::getVolumeType, IVolumeType::getStackStreamCodec);

	IVolumeType getVolumeType();

	int getAmount();

	IVolumeStackWrapper copy();

	IVolumeStackWrapper withAmount(int amount);

	boolean isEmpty();

	void saveAEKey(ValueOutput output);

	Component getDisplayName();

	Component getAmountDesc();

	List<Component> getTooltip();
}
