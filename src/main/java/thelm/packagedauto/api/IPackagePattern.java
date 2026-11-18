package thelm.packagedauto.api;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public interface IPackagePattern {

	static final MapCodec<IPackagePattern> MAP_CODEC = RecordCodecBuilder.mapCodec(instance->instance.group(
			IPackageRecipeInfo.MAP_CODEC.forGetter(IPackagePattern::getRecipeInfo),
			Codec.BYTE.fieldOf("index").forGetter(p->(byte)p.getIndex())).
			apply(instance, (r, i)->r.getPatterns().get(i)));
	static final Codec<IPackagePattern> CODEC = Codec.lazyInitialized(MAP_CODEC::codec);
	static final StreamCodec<RegistryFriendlyByteBuf, IPackagePattern> STREAM_CODEC = StreamCodec.composite(
			IPackageRecipeInfo.STREAM_CODEC, IPackagePattern::getRecipeInfo,
			ByteBufCodecs.BYTE, p->(byte)p.getIndex(),
			(r, i)->r.getPatterns().get(i));

	IPackageRecipeInfo getRecipeInfo();

	int getIndex();

	List<ItemStack> getInputs();

	ItemStack getOutput();
}
