package thelm.packagedauto.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record SettingsClonerData(String type, CompoundTag data, GlobalPos globalPos) {

	public static final Codec<SettingsClonerData> CODEC = RecordCodecBuilder.create(instance->instance.group(
			Codec.STRING.fieldOf("type").forGetter(SettingsClonerData::type),
			CompoundTag.CODEC.fieldOf("data").forGetter(SettingsClonerData::data),
			GlobalPos.MAP_CODEC.forGetter(SettingsClonerData::globalPos)).
			apply(instance, SettingsClonerData::new));
	public static final StreamCodec<ByteBuf, SettingsClonerData> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, SettingsClonerData::type,
			ByteBufCodecs.COMPOUND_TAG, SettingsClonerData::data,
			GlobalPos.STREAM_CODEC, SettingsClonerData::globalPos,
			SettingsClonerData::new);

	public SettingsClonerData(String type, CompoundTag data, ResourceKey<Level> dimension, BlockPos blockPos) {
		this(type, data, GlobalPos.of(dimension, blockPos));
	}

	public String type() {
		return type;
	}

	public CompoundTag data() {
		return data;
	}

	public GlobalPos globalPos() {
		return globalPos;
	}

	public ResourceKey<Level> dimension() {
		return globalPos.dimension();
	}

	public BlockPos blockPos() {
		return globalPos.pos();
	}

	public int x() {
		return blockPos().getX();
	}

	public int y() {
		return blockPos().getY();
	}

	public int z() {
		return blockPos().getZ();
	}
}
