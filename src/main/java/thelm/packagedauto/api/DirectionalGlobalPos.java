package thelm.packagedauto.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record DirectionalGlobalPos(GlobalPos globalPos, Direction direction) {

	public static final MapCodec<DirectionalGlobalPos> MAP_CODEC = RecordCodecBuilder.mapCodec(instance->instance.group(
			GlobalPos.MAP_CODEC.forGetter(DirectionalGlobalPos::globalPos),
			Direction.CODEC.fieldOf("direction").forGetter(DirectionalGlobalPos::direction)).
			apply(instance, DirectionalGlobalPos::new));
	public static final Codec<DirectionalGlobalPos> CODEC = Codec.lazyInitialized(MAP_CODEC::codec);
	public static final StreamCodec<ByteBuf, DirectionalGlobalPos> STREAM_CODEC = StreamCodec.composite(
			GlobalPos.STREAM_CODEC, DirectionalGlobalPos::globalPos,
			Direction.STREAM_CODEC, DirectionalGlobalPos::direction,
			DirectionalGlobalPos::new);

	public DirectionalGlobalPos(ResourceKey<Level> dimension, BlockPos blockPos, Direction direction) {
		this(GlobalPos.of(dimension, blockPos), direction);
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
