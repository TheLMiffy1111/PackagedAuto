package thelm.packagedauto.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public class PacketStreamCodecs {

	private PacketStreamCodecs() {}

	public static final StreamCodec<FriendlyByteBuf, Vec3> VEC3 = StreamCodec.of(FriendlyByteBuf::writeVec3, FriendlyByteBuf::readVec3);
	public static final StreamCodec<ByteBuf, Integer> UNSIGNED_BYTE = StreamCodec.of(ByteBuf::writeByte, b->(int)b.readUnsignedByte());
	public static final StreamCodec<ByteBuf, Integer> UNSIGNED_MEDIUM = StreamCodec.of(ByteBuf::writeMedium, ByteBuf::readUnsignedMedium);
}
